package com.github.charmasaur.alpsinsects.model;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Stores data about a species.
 */
public class Species {
  private String label;
  private String sublabel;
  private String searchText;
  private String squareThumbnail;
  private String description;
  private String order;
  private String family;
  private String genus;
  private String species;
  private String license;
  private String licenseLink;
  private ArrayList<Images> images;

  public Species() {}

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getSublabel() {
    return sublabel;
  }

  public void setSublabel(String sublabel) {
    this.sublabel = sublabel;
  }

  public String getSearchText() {
    return searchText;
  }

  public void setSearchText(String searchText) {
    this.searchText = searchText;
  }

  public String getSquareThumbnail() {
    return squareThumbnail;
  }

  public void setSquareThumbnail(String squareThumbnail) {
    this.squareThumbnail = squareThumbnail;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getOrder() {
    return order;
  }

  public void setOrder(String order) {
    this.order = order;
  }

  public String getFamily() {
    return family;
  }

  public void setFamily(String family) {
    this.family = family;
  }

  public String getGenus() {
    return genus;
  }

  public void setGenus(String genus) {
    this.genus = genus;
  }

  public String getSpecies() {
    return species;
  }

  public void setSpecies(String species) {
    this.species = species;
  }

  public String getLicense() {
    return license;
  }

  public void setLicense(String license) {
    this.license = license;
  }

  public String getLicenseLink() {
    return licenseLink;
  }

  public void setLicenseLink(String licenseLink) {
    this.licenseLink = licenseLink;
  }

  public ArrayList<Images> getImages() {
    return images;
  }

  public void setImages(ArrayList<Images> images) {
    this.images = images;
  }

  @Override
  public String toString() {
    return "Species [label=" + label + "]";
  }
}
