FROM centos:7

MAINTAINER Cao Hiep Hung <hung.cao@gooddata.com>

LABEL name="Checklist xvfb image based on CentOS-7" \
      vendor="CentOS" \
      license="GPLv2"

ADD docker-src/epel.repo /etc/yum.repos.d/
ADD docker-src/intgdcipa_ca.crt /etc/pki/ca-trust/source/anchors/intgdcipa_ca.crt
ADD docker-src/gooddata-koji.repo /etc/yum.repos.d/
ADD docker-src/RPM-GPG-KEY-EPEL-7 /etc/pki/rpm-gpg/
ADD docker-src/RPM-GPG-KEY-CentOS-7 /etc/pki/rpm-gpg

RUN set -x && \
    yum install --setopt=tsflags=nodocs -y xorg-x11-server-Xvfb xorg-x11-xinit tigervnc-server \
		chromedriver-55.0.2883.87-1.el7.1 chromium-52.0.2743.0-1* firefox-45.0.2-1 \
		dejavu-sans-fonts dejavu-sans-mono-fonts dejavu-serif-fonts phantomjs maven-bin gcc make which unzip && \
    update-ca-trust enable && update-ca-trust && \
    curl -L -v https://github.com/mozilla/geckodriver/releases/download/v0.14.0/geckodriver-v0.14.0-linux64.tar.gz > geckodriver-v0.14.0-linux64.tar.gz && \
    tar xvzf geckodriver-v0.14.0-linux64.tar.gz && \
    echo "checklist_image" | md5sum |cut -f1 -d\ > /etc/machine-id && \
    mv geckodriver /usr/bin/ && \
    ln -s /opt/firefox/firefox /usr/bin/firefox

