FROM docker-registry.na.intgdc.com/gdc_base_co7:latest

LABEL name="Checklist xvfb image based on CentOS-7" \
      maintainer="Silent Assassins Scrum <scrumsa@gooddata.com>" \
      vendor="CentOS" \
      license="GPLv2"

RUN set -x && \
    yum clean all && \
    yum install --setopt=tsflags=nodocs -y xorg-x11-server-Xvfb xorg-x11-xinit \
		chromedriver-2.30-1* chromium-58.0.3029.114-1* firefox-45.0.2-1 \
		dejavu-sans-fonts dejavu-sans-mono-fonts dejavu-serif-fonts phantomjs maven-bin which && \
    echo "checklist_image" | md5sum |cut -f1 -d\ > /etc/machine-id && \
    ln -s /opt/firefox/firefox /usr/bin/firefox
