FROM ubuntu:20.04

LABEL authors="Laise Florentino (lcf@ebi.ac.uk), Matthias Blum (mblum@ebi.ac.uk)"

ARG VERSION
ENV TZ=Europe/London

RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime \
 && echo $TZ > /etc/timezone \
 && apt-get update -y \
 && apt-get install -y --no-install-recommends \
    libc6-i386 \
    libgomp1 \
    libpcre2-dev \
    openjdk-11-jre-headless \
    perl-doc \
    python3.8 \
    wget \
 && ln -s /usr/bin/python3.8 /usr/bin/python3 \
 && ln -s /usr/bin/python3.8 /usr/bin/python \
 && rm -rf /var/lib/apt/lists/*

WORKDIR /opt
RUN wget ftp://ftp.ebi.ac.uk/pub/software/unix/iprscan/5/${VERSION}/alt/interproscan-core-${VERSION}.tar.gz && \
    wget ftp://ftp.ebi.ac.uk/pub/software/unix/iprscan/5/${VERSION}/alt/interproscan-core-${VERSION}.tar.gz.md5 && \
    md5sum -c interproscan-core-${VERSION}.tar.gz.md5 && \
    tar -zxf interproscan-core-${VERSION}.tar.gz && \
    rm interproscan-core-${VERSION}.tar.gz* && \
    mv /opt/interproscan-${VERSION} /opt/interproscan

WORKDIR /opt/interproscan
RUN sed -i 's|^temporary\.file\.directory=temp/|temporary.file.directory=/temp/|' interproscan.properties \
 && echo "binary.phobius.pl.path=/opt/interproscan/licensed/phobius/phobius.pl" >> interproscan.properties \
 && echo "binary.signalp.path=/opt/interproscan/licensed/signalp/signalp" >> interproscan.properties \
 && echo "signalp.perl.library.dir=/opt/interproscan/licensed/signalp/lib" >> interproscan.properties \
 && echo "binary.tmhmm.path=/opt/interproscan/licensed/tmhmm/decodeanhmm.Linux_x86_64" >> interproscan.properties \
 && echo "tmhmm.model.path=/opt/interproscan/licensed/tmhmm/TMHMM2.0.model" >> interproscan.properties

ENTRYPOINT ["/opt/interproscan/interproscan.sh"]
