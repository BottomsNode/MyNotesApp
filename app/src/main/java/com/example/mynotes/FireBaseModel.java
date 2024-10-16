package com.example.mynotes;

public class FireBaseModel {

    // Private fields for the title and content of the note
    private String title;   // The title of the note
    private String content; // The content of the note

    // Constructor to initialize the FireBaseModel with title and content
    public FireBaseModel(String title, String content) {
        this.title = title;   // Set the title
        this.content = content; // Set the content
    }

    // Getter method for the title
    public String getTitle() {
        return title; // Return the title of the note
    }

    // Setter method for the title
    public void setTitle(String title) {
        this.title = title; // Update the title of the note
    }

    // Getter method for the content
    public String getContent() {
        return content; // Return the content of the note
    }

    // Setter method for the content
    public void setContent(String content) {
        this.content = content; // Update the content of the note
    }
}
