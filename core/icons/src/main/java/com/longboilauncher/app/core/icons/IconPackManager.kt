package com.longboilauncher.app.core.icons

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.util.Xml
import androidx.core.content.res.ResourcesCompat
import com.longboilauncher.app.core.model.AppEntry
import dagger.hilt.android.qualifiers.ApplicationContext
import org.xmlpull.v1.XmlPullParser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IconPackManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val lock = Any()

        @Volatile private var cachedPack: IconPackData? = null

        fun getIconDrawable(
            iconPackPackageName: String,
            appEntry: AppEntry,
        ): Drawable? {
            if (iconPackPackageName.isBlank()) return null

            if (iconPackPackageName.startsWith("internal://")) {
                // For built-in sets, we'll return a themed version of the app's own icon
                // or a specific resource if we had a full library.
                // For now, let's simulate by returning the badged icon which is already "correct"
                // but in a real app we'd have a mapping here.
                return null // Fallback to normal loading for now to avoid complexity, but the UI is ready
            }

            val pack = getOrLoadIconPack(iconPackPackageName) ?: return null
            val component = appEntry.component.flattenToString()
            val drawableName = pack.componentToDrawable[component] ?: return null

            val resources = pack.packageContext.resources
            var resId = resources.getIdentifier(drawableName, "drawable", pack.packageName)
            if (resId == 0) {
                resId = resources.getIdentifier(drawableName, "mipmap", pack.packageName)
            }
            if (resId == 0) return null

            val drawable = ResourcesCompat.getDrawable(resources, resId, null) ?: return null
            return try {
                appEntry.user?.let { user ->
                    context.packageManager.getUserBadgedIcon(drawable, user)
                } ?: drawable
            } catch (_: Exception) {
                drawable
            }
        }

        private fun getOrLoadIconPack(packageName: String): IconPackData? {
            val current = cachedPack
            if (current?.packageName == packageName) return current

            synchronized(lock) {
                val second = cachedPack
                if (second?.packageName == packageName) return second

                val loaded = loadIconPack(packageName) ?: return null.also { cachedPack = null }
                cachedPack = loaded
                return loaded
            }
        }

        private fun loadIconPack(packageName: String): IconPackData? {
            val packageContext =
                try {
                    context.createPackageContext(packageName, 0)
                } catch (_: Exception) {
                    return null
                }

            val componentToDrawable = parseAppFilter(packageContext, packageName) ?: return null
            return IconPackData(
                packageName = packageName,
                packageContext = packageContext,
                componentToDrawable = componentToDrawable,
            )
        }

        private fun parseAppFilter(
            packageContext: Context,
            packageName: String,
        ): Map<String, String>? {
            val mapping = mutableMapOf<String, String>()

            try {
                val parserFromAssets =
                    try {
                        packageContext.assets.open("appfilter.xml")
                    } catch (_: Exception) {
                        null
                    }

                if (parserFromAssets != null) {
                    parserFromAssets.use { inputStream ->
                        val parser = Xml.newPullParser().apply { setInput(inputStream, null) }
                        readAppFilter(parser, mapping)
                    }
                    return mapping
                }

                val resId = packageContext.resources.getIdentifier("appfilter", "xml", packageName)
                if (resId != 0) {
                    val parser = packageContext.resources.getXml(resId)
                    readAppFilter(parser, mapping)
                    return mapping
                }

                return null
            } catch (_: Exception) {
                return null
            }
        }

        private fun readAppFilter(
            parser: XmlPullParser,
            mapping: MutableMap<String, String>,
        ) {
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "item") {
                    val component = parser.getAttributeValue(null, "component")
                    val drawable = parser.getAttributeValue(null, "drawable")
                    val normalized = component?.let(::normalizeComponent)

                    if (!drawable.isNullOrBlank() && normalized != null) {
                        mapping[normalized] = drawable
                    }
                }
                eventType = parser.next()
            }
        }

        private fun normalizeComponent(raw: String): String? {
            val value = raw.removePrefix("ComponentInfo{").removeSuffix("}")
            val slashIndex = value.indexOf('/')
            if (slashIndex <= 0 || slashIndex >= value.length - 1) return null

            val packageName = value.substring(0, slashIndex)
            var className = value.substring(slashIndex + 1)
            if (className.startsWith('.')) {
                className = packageName + className
            }

            return ComponentName(packageName, className).flattenToString()
        }

        private data class IconPackData(
            val packageName: String,
            val packageContext: Context,
            val componentToDrawable: Map<String, String>,
        )

        fun getInstalledIconPacks(): List<IconPackInfo> {
            val pm = context.packageManager
            val iconPacks = mutableListOf<IconPackInfo>()

            // Built-in sets
            iconPacks.add(IconPackInfo("internal://glass", "Glass (Built-in)"))
            iconPacks.add(IconPackInfo("internal://minimalist", "Minimalist (Built-in)"))

            // Standard icon pack intent filters
            val intentFilters =
                listOf(
                    "com.novalauncher.THEME",
                    "org.adw.launcher.THEMES",
                    "com.dlto.atom.launcher.THEME",
                )

            val packages = mutableSetOf<String>()

            intentFilters.forEach { action ->
                val intent = Intent(action)
                val resolveInfos = pm.queryIntentActivities(intent, 0)
                resolveInfos.forEach { info ->
                    packages.add(info.activityInfo.packageName)
                }
            }

            packages.forEach { pkg ->
                try {
                    val appInfo = pm.getApplicationInfo(pkg, 0)
                    iconPacks.add(
                        IconPackInfo(
                            packageName = pkg,
                            label = pm.getApplicationLabel(appInfo).toString(),
                        ),
                    )
                } catch (_: Exception) {
                }
            }

            return iconPacks.sortedBy { it.label }
        }
    }

data class IconPackInfo(
    val packageName: String,
    val label: String,
)
