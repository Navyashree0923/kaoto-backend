package io.zimara.backend.model;

/**
 * 🐱class View
 * Represents a possible view the frontend can use to view data and metadata.
 */
public abstract class View {
    /*
     * 🐱property type: String
     *
     * Type of the view. It could be an integration view, a connector view,...
     */
    public abstract String getType();

    /*
     * 🐱property name: String
     *
     * Human name for the view
     */
    public abstract String getName();
}