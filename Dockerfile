FROM docker-registry.na.intgdc.com/gdc_base_co7:latest

LABEL name="Checklist xvfb image based on CentOS-7" \
      maintainer="Silent Assassins Scrum <scrumsa@gooddata.com>" \
      vendor="CentOS" \
      license="GPLv2"

COPY google-chrome.repo /etc/yum.repos.d/google-chrome.repo

ARG CHROME_DRIVER_VERSION=latest
ARG FIREFOX_VERSION=58.0.2
ARG GECKODRIVER_VERSION=0.20.0

# Commands are chained to squeeze the image size (~950 MB).

# Install latest Firefox and Chrome browsers.
# The procedure is taken from official Selenium Dockerfiles.
# Note: Firefox is installed with yum to resolve dependencies, but it is ESR version,
# so then it is replaced with specific version.
RUN set -x && \
    rpm --import https://dl-ssl.google.com/linux/linux_signing_key.pub && \
    yum clean all && \
    yum install --setopt=tsflags=nodocs -y xorg-x11-server-Xvfb xorg-x11-xinit \
                dejavu-sans-fonts dejavu-sans-mono-fonts dejavu-serif-fonts \
                phantomjs maven-bin which curl unzip bzip2 \
                google-chrome-stable firefox && \
    echo "checklist_image" | md5sum |cut -f1 -d\ > /etc/machine-id && \
    curl https://download-installer.cdn.mozilla.net/pub/firefox/releases/$FIREFOX_VERSION/linux-x86_64/en-US/firefox-$FIREFOX_VERSION.tar.bz2 > /tmp/firefox.tar.bz2 && \
    yum remove -y firefox && \
    rm -rf /opt/firefox && \
    tar -C /opt -xjf /tmp/firefox.tar.bz2 && \
    rm /tmp/firefox.tar.bz2 && \
    mv /opt/firefox /opt/firefox-$FIREFOX_VERSION && \
    ln -fs /opt/firefox-$FIREFOX_VERSION/firefox /usr/bin/firefox && \
    yum remove -y bzip2 && yum clean all && rm -rf /var/cache/yum

# Install Chromedriver and Geckodriver.
# The procedure is taken from official Selenium Dockerfiles.
RUN CD_VERSION=$(if [ ${CHROME_DRIVER_VERSION:-latest} = "latest" ]; then echo $(curl https://chromedriver.storage.googleapis.com/LATEST_RELEASE); else echo $CHROME_DRIVER_VERSION; fi) && \
    echo "Using chromedriver version: "$CD_VERSION && \
    curl https://chromedriver.storage.googleapis.com/$CD_VERSION/chromedriver_linux64.zip > /tmp/chromedriver_linux64.zip && \
    unzip /tmp/chromedriver_linux64.zip -d /opt/selenium && \
    rm /tmp/chromedriver_linux64.zip && \
    mv /opt/selenium/chromedriver /opt/selenium/chromedriver-$CD_VERSION && \
    chmod 755 /opt/selenium/chromedriver-$CD_VERSION && \
    ln -fs /opt/selenium/chromedriver-$CD_VERSION /usr/bin/chromedriver && \
    ln -fs /opt/google/chrome/chrome /usr/bin/chrome && \
    curl -L https://github.com/mozilla/geckodriver/releases/download/v$GECKODRIVER_VERSION/geckodriver-v$GECKODRIVER_VERSION-linux64.tar.gz > /tmp/geckodriver.tar.gz && \
    tar -C /opt -zxf /tmp/geckodriver.tar.gz && \
    rm /tmp/geckodriver.tar.gz && \
    mv /opt/geckodriver /opt/geckodriver-$GECKODRIVER_VERSION && \
    chmod 755 /opt/geckodriver-$GECKODRIVER_VERSION && \
    ln -fs /opt/geckodriver-$GECKODRIVER_VERSION /usr/bin/geckodriver

