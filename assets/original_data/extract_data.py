#!/bin/python

import csv
import json

species_filename = "Species.csv";
groups_filename = "Orders.csv";
version = 1.0

species_file = open(species_filename);
species_reader = csv.reader(species_file);
# Skip header row.
next(species_reader);
species_entries = [];

# TODO: Read species data.


groups_file = open(groups_filename);
groups_reader = csv.reader(groups_file);
# Skip header row.
next(groups_reader);
group_entries = []

for (order, group, icon_w, icon_d, icon_credit, license_link, desc, other) in groups_reader:
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

print(json.dumps({ "groups_data" : group_entries}, sort_keys=True, indent=2, separators=(',',': ')))
