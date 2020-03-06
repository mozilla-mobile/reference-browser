# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

# If a command fails then do not proceed and fail this script too.
set -ex

# Install dependencies (TODO: Move to Docker image)
apt-get install -y brew
brew install hub

export BRANCH="ac-update"
export GITHUB_USER="MickeyMoz"
export EMAIL="sebastian@mozilla.com"
export REPO="reference-browser"

git config --global user.email "$EMAIL"
git config --global user.name "$GITHUB_USER"

# Fetching latest version
LATEST_VERSION=$(curl https://nightly.maven.mozilla.org/maven2/org/mozilla/components/browser-engine-gecko-nightly/maven-metadata.xml | sed -ne '/latest/{s/.*<latest>\(.*\)<\/latest>.*/\1/p;q;}')

# Updating version file
sed -i "s/VERSION = \".*\"/VERSION = \"$LATEST_VERSION\"/g" "buildSrc/src/main/java/AndroidComponents.kt"

# Create a branch and commit local changes
git checkout -b "$BRANCH"
git add buildSrc/src/main/java/AndroidComponents.kt
git commit -m \
	"Update Android Components version to $LATEST_VERSION." \
	--author="MickeyMoz <sebastian@mozilla.com>" \
|| { echo "No new Android Components version ($LATEST_VERSTION) available"; exit 0; }


# From here on we do not want to print the commands since they contain tokens
set +x

export GITHUB_TOKEN=$(cat .github_token)

# Push changes to GitHub
echo "Pushing branch to GitHub"
URL="https://${GITHUB_USER}:${GITHUB_TOKEN}@github.com/$GITHUB_USER/$REPO/"
# XXX git sometimes leaks the URL including the token when the network request failed (regardless of --quiet).
git push --force --no-verify --quiet "$URL" "$BRANCH" > /dev/null 2>&1 || echo "Failed ($?)"

# Open a PR if needed
if [[ $(hub pr list --head "$GITHUB_USER:$BRANCH") ]]; then
    echo "There's already an open PR."
else
    echo "No PR found. Opening new PR."
    hub pull-request --base master --head "$GITHUB_USER:$BRANCH" --no-edit -m "Update Android Components version"
fi

unset GITHUB_TOKEN
