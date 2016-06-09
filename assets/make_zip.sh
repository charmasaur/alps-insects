#!/bin/bash
zip -r new.zip assets/
echo "$(stat --printf="%s" new.zip)"
