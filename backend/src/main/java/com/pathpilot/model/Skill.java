package com.pathpilot.model;

import java.util.Objects;

/**
 * Encapsulated domain entity representing a canonical technical or professional skill.
 */
public class Skill {
    private Long id;
    private String name;
    private String category;
    private String domain;
    private String description;

    public Skill() {}

    public Skill(Long id, String name, String category, String domain, String description) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.domain = domain;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "Skill name must not be null");
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Skill skill = (Skill) o;
        return Objects.equals(name != null ? name.toLowerCase() : null,
                skill.name != null ? skill.name.toLowerCase() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name != null ? name.toLowerCase() : null);
    }

    @Override
    public String toString() {
        return "Skill{" + "id=" + id + ", name='" + name + '\'' + ", category='" + category + '\'' + '}';
    }
}
