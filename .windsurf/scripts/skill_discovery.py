#!/usr/bin/env python3
"""
Skill Discovery Script for Windsurf
Discovers all available skills and their metadata
"""

import os
import yaml
from pathlib import Path
import json
import re

def extract_frontmatter(skill_file):
    """Extract YAML frontmatter from SKILL.md"""
    with open(skill_file, 'r', encoding='utf-8') as f:
        content = f.read()

    # Extract YAML between --- markers
    if content.startswith('---'):
        try:
            end = content.find('---', 3)
            if end != -1:
                yaml_content = content[3:end].strip()
                return yaml.safe_load(yaml_content)
        except:
            pass

    return {}

def extract_description(skill_file):
    """Extract description from skill file"""
    with open(skill_file, 'r', encoding='utf-8') as f:
        content = f.read()

    # Try to get from frontmatter first
    frontmatter = extract_frontmatter(skill_file)
    if 'description' in frontmatter:
        return frontmatter['description']

    # Fallback: first paragraph after frontmatter
    lines = content.split('\n')
    description = []
    in_frontmatter = False

    for line in lines:
        if line.strip() == '---':
            if not in_frontmatter:
                in_frontmatter = True
                continue
            else:
                in_frontmatter = False
                continue

        if not in_frontmatter and line.strip():
            if line.startswith('#'):
                continue
            description.append(line.strip())
            if len(description) > 2:  # Limit to first few lines
                break

    return ' '.join(description) if description else "No description available"

def find_skills(skills_dir):
    """Find all skills in the skills directory"""
    skills = []
    skills_path = Path(skills_dir)

    if not skills_path.exists():
        return skills

    for skill_dir in skills_path.iterdir():
        if skill_dir.is_dir():
            skill_file = skill_dir / 'SKILL.md'
            if skill_file.exists():
                frontmatter = extract_frontmatter(skill_file)

                skill = {
                    'name': frontmatter.get('name', skill_dir.name),
                    'path': str(skill_dir),
                    'skill_file': str(skill_file),
                    'description': extract_description(skill_file),
                    'directory': skill_dir.name
                }

                # Add additional metadata if available
                if 'when_to_use' in frontmatter:
                    skill['when_to_use'] = frontmatter['when_to_use']

                skills.append(skill)

    return sorted(skills, key=lambda x: x['name'])

def format_skills_list(skills):
    """Format skills as markdown list"""
    if not skills:
        return "No skills found."

    output = ["## Available Skills\n"]

    for skill in skills:
        output.append(f"### {skill['name']}")
        output.append(f"**Path**: `.windsurf/skills/{skill['directory']}/`")
        output.append(f"**Description**: {skill['description']}")

        if 'when_to_use' in skill:
            output.append(f"**When to use**: {skill['when_to_use']}")

        output.append("")

    return "\n".join(output)

def main():
    """Main function"""
    # Find skills directory relative to script
    script_dir = Path(__file__).parent
    skills_dir = script_dir.parent / 'skills'

    # Discover skills
    skills = find_skills(skills_dir)

    # Output as JSON for programmatic use
    if '--json' in os.sys.argv:
        print(json.dumps(skills, indent=2))
    else:
        # Output as markdown for human reading
        print(format_skills_list(skills))

    # Also create/update a skills index file
    index_file = script_dir.parent / 'SKILLS_INDEX.md'
    with open(index_file, 'w', encoding='utf-8') as f:
        f.write("# Skills Index\n\n")
        f.write(f"Total skills: {len(skills)}\n\n")
        f.write(format_skills_list(skills))

if __name__ == "__main__":
    main()
