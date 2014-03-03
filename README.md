# azureblob

finna upload some blob

## Usage

    % lein clean && lein uberjar
    % pushd /somewhere/with/blobs
    % java -jar /path/to/azureblob.jar \
    --name=es1       # the storage account name \
    --key=fwabTQeFvQb9/45oklR9BAH4k7e1WsU7hYZJbFoLCdCMF6BCsE/85/WqlEbYBj4BkEj8+m9Z5Nrr9Lvob1zIaQ== # account secret from the "Manage Access Keys" dialog \
    --container=data # like a bucket on s3 \
    put              # only put for now, can sync if src is a dir \
    wiki3m           # source to sync \
    /                # destination key prefix (or name if source is a file)

## License

Copyright © 2014 Andrew A. Raines

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
