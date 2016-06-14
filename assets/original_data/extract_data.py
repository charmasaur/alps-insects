#!/bin/python

import csv
import json

def get_bool(entry):
    if entry == "y":
        return True;
    if entry == "n":
        return False;
    raise Exception("Unexpected boolean: " + entry);

def is_present(entry):
    return entry and not entry == "na"

def get_images(pictures, credits):
    images = [];
    for (picture, credit) in zip(pictures, credits):
        if is_present(picture):
            entry = {"filename" : picture};
            if is_present(credit):
                entry["credit"] = credit;
            images.append(entry);
    return images

def assert_present(entry, name):
    if not is_present(entry):
        raise Exception("Entry not present: " + name);

species_filename = "Species.csv";
groups_filename = "Orders.csv";
version = 1.0
output_filename = "output.json";

species_file = open(species_filename);
species_reader = csv.reader(species_file);
# Skip header row.
next(species_reader);
species_entries = [];

for (ide, order, family, genus, scientific_name, label, sublabel, italicise_sublabel, description,
        license_link, license, thumb, p1, c1, p2, c2, p3, c3, p4, c4, p5, c5, p6,
        c6) in species_reader:
    print("Reading species: " + ide);

    # Ensure that required fields are present.
    assert_present(ide, "ID");
    assert_present(order, "Order");
    assert_present(label, "Label");
    assert_present(sublabel, "Sublabel");
    assert_present(italicise_sublabel, "Italicise sublabel");
    assert_present(description, "Description");
    assert_present(thumb, "Thumbnail");

    entry = {
            "identifier" : ide,
            "taxaOrder" : order,
            "label" : label,
            "sublabel" : sublabel, # TODO: Italicise here?
            "italiciseSublabel" : get_bool(italicise_sublabel),
            "description" : description,
            "squareThumbnail" : thumb,
            "images" : get_images([p1, p2, p3, p4, p5, p6], [c1, c2, c3, c4, c5, c6])};
            
    # TODO: Better to leave this fields unset or set them to the empty string?
    if is_present(family):
        entry["taxaFamily"] = family;
    if is_present(genus):
        entry["taxaGenus"] = genus;
    if is_present(scientific_name):
        entry["scientificName"] = scientific_name;
    if is_present(license_link):
        entry["licenseLink"] = license_link;

    species_entries.append(entry);


groups_file = open(groups_filename);
groups_reader = csv.reader(groups_file);
# Skip header row.
next(groups_reader);
group_entries = []

for (order, group, icon_w, icon_d, icon_credit, license_link, desc, other) in groups_reader:
    print("Reading group: " + order);
    
    # Ensure that required fields are present.
    assert_present(order, "Order");
    assert_present(group, "Group");
    assert_present(icon_w, "Icon w");
    assert_present(icon_d, "Icon d");
    assert_present(icon_credit, "Icon credit");
    entry = {
            "order" : order,
            "label" : group,
            "iconWhiteFilename" : icon_w + '.png',
            "iconDarkFilename" : icon_d + '.png',
            "iconCredit" : icon_credit,
            "description" : desc};

    if not license_link == "na":
        entry["licenseLink"] = license_link;
    group_entries.append(entry);


output_file = open(output_filename, "w");
output_file.write(json.dumps(
        { "species_data" : species_entries, "groups_data" : group_entries, "version" : version},
        sort_keys=True, indent=2, separators=(',',': ')));
