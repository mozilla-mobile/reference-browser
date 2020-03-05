# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

# If a command fails then do not proceed and fail this script too.
set -ex

BRANCH="ac-update"
USER="MickeyMoz"
EMAIL="sebastian@mozilla.com"
REPO="reference-browser"

git config --global user.email $EMAIL
git config --global user.name $USER

COMPONENT_TO_WATCH='browser-engine-gecko-nightly'
MAVEN_URL="https://nightly.maven.mozilla.org/maven2/org/mozilla/components/$COMPONENT_TO_WATCH"

# Fetch latest version
LATEST_VERSION=`curl "$MAVEN_URL/maven-metadata.xml" | sed -ne '/latest/{s/.*<latest>\(.*\)<\/latest>.*/\1/p;q;}'`

# Check the latest version was uploaded by Mozilla
LATEST_POM_URL="$MAVEN_URL/$LATEST_VERSION/$COMPONENT_TO_WATCH-$LATEST_VERSION.pom"
POM_FILE='component.pom'
$CURL "$LATEST_POM_URL" --output "$POM_FILE"
$CURL "$LATEST_POM_URL.asc" --output "$POM_FILE.asc"
gpg --verify "$POM_FILE.asc" "component.pom"

# Updating version file
sed -i "s/VERSION = \".*\"/VERSION = \"$LATEST_VERSION\"/g" "buildSrc/src/main/java/AndroidComponents.kt"

# Create a branch and commit local changes
git checkout -b $BRANCH
git add buildSrc/src/main/java/AndroidComponents.kt
git commit -m \
	"Update Android Components version to $LATEST_VERSION." \
	--author="MickeyMoz <sebastian@mozilla.com>" \
|| { echo "No new Android Components version ($LATEST_VERSTION) available"; exit 0; }


# From here on we do not want to print the commands since they contain tokens
# set +x

GITHUB_TOKEN=`cat .github_token`
URL="https://$USER:$GITHUB_TOKEN@github.com/$USER/$REPO/"

# Push changes to GitHub
echo "Pushing branch to GitHub"
git push -f --no-verify --quiet $URL $BRANCH > /dev/null 2>&1 || echo "Failed ($?)"

# Open a PR if needed
if [[ $(hub pr list -h $USER:$BRANCH) ]]; then
    echo "There's already an open PR."
else
    echo "No PR found. Opening new PR."
    hub pull-request -b master -h MickeyMoz:ac-update --no-edit -m "Update Android Components version"
fi
