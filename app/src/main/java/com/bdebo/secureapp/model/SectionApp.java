package com.bdebo.secureapp.model;

/**
 * This model class is used to hold Header Section of any app
 */
public class SectionApp implements Item {

    /**
     * Holds the title of the Header section
     */
    String title;

    public SectionApp() {
    }

    public SectionApp(String title) {
        this.title = title;

    }

    /**
     * Holds the title of the Header section
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the title of the Header section
     */
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean isSection() {
        return true;
    }
}
