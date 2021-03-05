WORKSPACE=".papermc"
MC_VERSION="1.16.4"
PAPER_BUILD="325"

GROUPID=$(mvn org.apache.maven.plugins:maven-help-plugin:3.1.0:evaluate -Dexpression=project.groupId -q -DforceStdout)
ARTIFACTID=$(mvn org.apache.maven.plugins:maven-help-plugin:3.1.0:evaluate -Dexpression=project.artifactId -q -DforceStdout)
VERSION=$(mvn org.apache.maven.plugins:maven-help-plugin:3.1.0:evaluate -Dexpression=project.version -q -DforceStdout)

JARNAME=$ARTIFACTID-$VERSION-jar-with-dependencies

compiledCorrectly=false

function compile {
    rm -rf ~/$WORKSPACE/plugins/$ARTIFACTID/lang/
    rm ~/$WORKSPACE/plugins/$ARTIFACTID.jar
    rm ./target/$ARTIFACTID*.jar
    if mvn verify && mvn assembly:assembly -DdescriptorId=jar-with-dependencies && mvn install:install-file \
        -Dfile=./target/$ARTIFACTID-$VERSION.jar \
        -DgroupId=$GROUPID \
        -DartifactId=$ARTIFACTID \
        -Dversion=$VERSION; then
        cp ./target/$JARNAME.jar ./target/$ARTIFACTID.jar
        compiledCorrectly=true
    fi
}

function run {
    
    cp ./target/$JARNAME.jar ~/$WORKSPACE/plugins/$ARTIFACTID.jar

    cd || exit # Moving to the user folder or exit if it fails.

    # Checking the workspace folder availability.
    if [ ! -d $WORKSPACE ]; then
        # Create the workspace folder.
        mkdir $WORKSPACE
    fi

    cd $WORKSPACE || exit # Moving to the workspace fodler or exit if it fails.

    # Check for the paper executable
    PAPER_JAR="paper-$MC_VERSION-$PAPER_BUILD.jar"
    PAPER_LNK="https://papermc.io/api/v1/paper/$MC_VERSION/$PAPER_BUILD/download"

    if [ ! -f $PAPER_JAR ]; then
        wget -O $PAPER_JAR $PAPER_LNK
    fi

    java -jar $PAPER_JAR nogui

    rm ~/$WORKSPACE/plugins/$ARTIFACTID.jar

}

if [ $# = 0 ]; then
    compile
    if [ $compiledCorrectly = true ]; then
        run
    fi
else
    argLower=$(echo "$1" | tr '[:upper:]' '[:lower:]')
    if [ $argLower = "--nocompile" ] || [ $argLower = "-nc" ]; then
        run
    elif [ $argLower = "--norun" ] || [ $argLower = "-nr" ]; then
        compile
    fi
fi
