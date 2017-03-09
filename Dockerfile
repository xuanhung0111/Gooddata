FROM docker-registry-el7.na.intgdc.com/gdc_base_co7:latest

LABEL name="Checklist xvfb image based on CentOS-7" \
      maintainer="Cao Hiep Hung <hung.cao@gooddata.com>" \
      vendor="CentOS" \
      license="GPLv2"

RUN set -x && \
    yum clean all && \
    yum install --setopt=tsflags=nodocs -y xorg-x11-server-Xvfb xorg-x11-xinit tigervnc-server \
		chromedriver chromium firefox \
		dejavu-sans-fonts dejavu-sans-mono-fonts dejavu-serif-fonts phantomjs maven-bin which && \
    echo "checklist_image" | md5sum |cut -f1 -d\ > /etc/machine-id
