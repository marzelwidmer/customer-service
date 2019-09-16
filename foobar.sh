#!/bin/bash
echo "CHANGELOG"
echo ----------------------
git tag -l  | while read TAG ; do
    echo
    if [ $NEXT ];then
        echo [$TAG]
        echo [$NEXT]
    else
        echo [$TAG]
    fi
    GIT_PAGER=cat git log --no-merges --format=" * %s" $TAG..$NEXT
    NEXT=$TAG
done
FIRST=$(git tag -l --sort=v:refname | head -1)
echo
echo [$FIRST]
GIT_PAGER=cat git log --no-merges --format=" * %s" $FIRST

#echo "CHANGELOG"
#echo ----------------------
#git tag -l --sort=v:refname | while read TAG ; do
#    echo
#    if [ $NEXT ];then
#        echo [$NEXT]
#        echo tag: $TAG
#        echo next: $NEXT
#        git log --no-merges --format=" * %s" $TAG..$NEXT
#        git log  $TAG..$NEXT --pretty=format:"  * %s"
#    else
#        echo "[Current]"
#    fi
#    NEXT=$TAG
#done

#git config --global alias.plog "log --graph --pretty=format:'%h -%d %s %n' --abbrev-commit --date=relative --branches"
#git log `git describe --tags --abbrev=0`..HEAD --pretty=format:"  * %s"

#git tag -l --sort=v:refname | while read TAG ; do
#    echo
#    if [ $NEXT ];then
#        echo [$NEXT]
#        for COMMIT in $COMMITS; do
#            # Get the subject of the current commit
#            SUBJECT=$(git log -1 ${COMMIT} --pretty=format:"%s" | grep -E '(feature:|minor:|feat:|chore:)')
#            echo $SUBJECT
#        done
#    fi
#    NEXT=$TAG
#done



#echo "CHANGELOG2"
#echo ----------------------
## Get a list of all tags in reverse order
## Assumes the tags are in version format like v1.2.3
#GIT_TAGS=$(git tag -l --sort=-version:refname)
#
## Make the tags an array
#TAGS=($GIT_TAGS)
#LATEST_TAG=${TAGS[0]}
#PREVIOUS_TAG=${TAGS[1]}


# If you want to specify your own two tags to compare, uncomment and enter them below
# LATEST_TAG=v0.23.1
# PREVIOUS_TAG=v0.22.0

# Get a log of commits that occured between two tags
# We only get the commit hash so we don't have to deal with a bunch of ugly parsing
# See Pretty format placeholders at https://git-scm.com/docs/pretty-formats
#COMMITS=$(git log $PREVIOUS_TAG..$LATEST_TAG --pretty=format:"%H")


#echo $LATEST_TAG
#echo $PREVIOUS_TAG
#echo $COMMITS


# Loop over each commit and look for merged pull requests
#for COMMIT in $COMMITS; do
#	# Get the subject of the current commit
#	SUBJECT=$(git log -1 ${COMMIT} --pretty=format:"%s" | grep -E '(feature:|minor:|feat:|chore:)')
#    echo $SUBJECT
#done

