#! /bin/bash
for var in "$@"
do
	java -Xms64M -Xmx64M -jar ../occurrence-cli.jar delete-dataset --conf config/occurrence-deleter.yaml --dataset-uuid $var
done
