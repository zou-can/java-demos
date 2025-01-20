ARG UBUNTU_VERSION=22.04
FROM ubuntu:$UBUNTU_VERSION

ARG DEBIAN_FRONTEND=noninteractive
ENV APT_KEY_DONT_WARN_ON_DANGEROUS_USAGE=DontWarn

COPY --chown=root:root docker/ubuntu22/sources.list /etc/apt/sources.list

# Update and install build requirements.
RUN apt update && apt upgrade -y && \
    # Basic build requirements from documentation
    apt-get install -y \
            autoconf \
            automake \
            bison \
            build-essential \
            flex \
            git \
            install-info \
            libc-ares-dev \
            libcap-dev \
            libelf-dev \
            libjson-c-dev \
            libpam0g-dev \
            libreadline-dev \
            libsnmp-dev \
            libsqlite3-dev \
            lsb-release \
            libtool \
            lcov \
            make \
            perl \
            pkg-config \
            python3-dev \
            python3-sphinx \
            screen \
            texinfo \
            tmux \
    && \
    # Protobuf build requirements
    apt-get install -y \
        libprotobuf-c-dev \
        protobuf-c-compiler \
    && \
    # Libyang2 extra build requirements
    apt-get install -y \
        cmake \
        libpcre2-dev \
    && \
    # GRPC extra build requirements
    apt-get install -y \
        libgrpc-dev \
        libgrpc++-dev \
        protobuf-compiler-grpc \
    && \
    # Runtime/triage/testing requirements
    apt-get install -y \
        curl \
        gdb \
        kmod \
        iproute2 \
        iputils-ping \
        liblua5.3-dev \
        libssl-dev \
        lua5.3 \
        net-tools \
        python3 \
        python3-pip \
        snmp \
        snmp-mibs-downloader \
        snmpd \
        sudo \
        time \
        tshark \
        valgrind \
        yodl \
      && \
    download-mibs && \
    wget https://raw.githubusercontent.com/FRRouting/frr-mibs/main/iana/IANA-IPPM-METRICS-REGISTRY-MIB -O /usr/share/snmp/mibs/iana/IANA-IPPM-METRICS-REGISTRY-MIB && \
    wget https://raw.githubusercontent.com/FRRouting/frr-mibs/main/ietf/SNMPv2-PDU -O /usr/share/snmp/mibs/ietf/SNMPv2-PDU && \
    wget https://raw.githubusercontent.com/FRRouting/frr-mibs/main/ietf/IPATM-IPMC-MIB -O /usr/share/snmp/mibs/ietf/IPATM-IPMC-MIB

# Install FRR built packages
RUN mkdir -p /etc/apt/keyrings && \
    curl -s -o /etc/apt/keyrings/frrouting.gpg https://deb.frrouting.org/frr/keys.gpg && \
    echo deb '[signed-by=/etc/apt/keyrings/frrouting.gpg]' https://deb.frrouting.org/frr \
        $(lsb_release -s -c) "frr-stable" > /etc/apt/sources.list.d/frr.list && \
    apt-get update && apt-get install -y librtr-dev libyang2-dev libyang2-tools

RUN mkdir -p /root/frr/

COPY --chown=root:root ../.. /root/frr/

RUN cd /root/frr/ && \
    ./bootstrap.sh && \
    ./configure \
       --prefix=/usr \
       --includedir=\${prefix}/include \
       --bindir=\${prefix}/bin \
       --sbindir=\${prefix}/lib/frr \
       --libdir=\${prefix}/lib/frr \
       --libexecdir=\${prefix}/lib/frr \
       --sysconfdir=/etc \
       --localstatedir=/var \
       --with-moduledir=\${prefix}/lib/frr/modules \
       --enable-configfile-mask=0640 \
       --enable-logfile-mask=0640 \
       --enable-rpki \
       --enable-multipath=64 \
       --enable-user=root \
       --enable-group=root \
       --enable-grpc \
       --enable-vty-group=root \
       --enable-snmp=agentx \
       --enable-scripting \
       --with-pkg-extra-version=10.2.1 && \
    make -j $(nproc) && \
    sudo make install && \
    sudo install -m 775 -d /var/log/frr && \
    sudo install -m 775 -d /etc/frr && \
    sudo install -m 640 tools/etc/frr/vtysh.conf /etc/frr/vtysh.conf && \
    sudo install -m 640 tools/etc/frr/frr.conf /etc/frr/frr.conf && \
    sudo install -m 640 tools/etc/frr/daemons.conf /etc/frr/daemons.conf && \
    sudo install -m 640 tools/etc/frr/daemons /etc/frr/daemons

RUN mkdir -p /var/run/frr && \
    sed -i '/^#net.ipv4.ip_forward=1/s/^#//' "/etc/sysctl.conf" && \
    sed -i '/^#net.ipv6.conf.all.forwarding=1/s/^#//' "/etc/sysctl.conf"

COPY --chmod=755 --chown=root:root docker/ubuntu22/docker-start /usr/sbin/docker-start

CMD ["/usr/sbin/docker-start"]