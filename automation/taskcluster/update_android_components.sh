# If a command fails then do not proceed and fail this script too.
set -ex

# hub pull-request -b master -h MickeyMoz:ac-update --no-edit
# 
# hub pull-request -b master -h MickeyMoz:ac-update --no-edit -m "Update Android Components version"

BRANCH="ac-update"
USER="MickeyMoz"
EMAIL="sebastian@mozilla.com"
REPO="reference-browser"

git config --global user.email $EMAIL
git config --global user.name $USER

# Fetching latest version
LATEST_VERSION=`curl https://nightly.maven.mozilla.org/maven2/org/mozilla/components/browser-engine-gecko-nightly/maven-metadata.xml | sed -ne '/latest/{s/.*<latest>\(.*\)<\/latest>.*/\1/p;q;}'`

# Updating version file
sed -i "" "s/VERSION = \".*\"/VERSION = \"$LATEST_VERSION\"/g" "buildSrc/src/main/java/AndroidComponents.kt"

# Create a branch and commit local changes
git checkout -b $BRANCH
git add buildSrc/src/main/java/AndroidComponents.kt
git commit -m \
	"Update Android Components version to $LATEST_VERSION." \
	--author="MickeyMoz <sebastian@mozilla.com>" \
|| { echo "No new Android Components version ($LATEST_VERSTION) available"; exit 0; }

# Get token for using GitHub
#python automation/taskcluster/helper/get-secret.py \
#    -s project/mobile/github \
#    -k botAccountToken \
#    -f .github_token \

# From here on we do not want to print the commands since they contain tokens
set +x

GITHUB_TOKEN=`cat .github_token`
URL="https://$USER:$GITHUB_TOKEN@github.com/$USER/$REPO/"

# Push changes to GitHub
echo "Pushing branch to GitHub"
git push  --no-verify --quiet $URL $BRANCH

# Open a PR if needed
if [[ $(hub pr list -h $USER:$BRANCH-update) ]]; then
    echo "There's already an open PR."
else
    echo "No PR found. Opening new PR."
    hub pull-request -b master -h MickeyMoz:ac-update --no-edit -m "Update Android Components version"
fi
