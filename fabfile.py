# -*- coding: utf-8 -*-

from fabric.api import run, abort, env, put, local, sudo
from fabric.decorators import runs_once
from fabric.context_managers import lcd, settings

from fabric.contrib.console import confirm

import os

war_file = "astrocats-0.1.0-SNAPSHOT-standalone.war"


def astrocats():
    env.environment = "astrocats"
    env.forward_agent = True
    env.port = 22
    env.hosts = ["162.213.39.149"]
    env.user = "cloudsigma"


@runs_once
def _check():
    if not confirm('deploy to %s?' % env.environment, default=False):
        abort('deployment is cancelled.')


def _clean(path):
    TARGETS = ['.DS_Store']

    for root, dirs, files in os.walk(path):
        for name in files:
            if name in TARGETS:
                os.remove(os.path.join(str(root), str(name)))


def deploy():
    _check()

    sudo(":")

    local("rm -f resources/public/public.tar.gz")

    local("lein clean")
    local("lein cljsbuild clean")
    local("lein cljsbuild once")
    local("lein ring-jetty uberwar")

    put("target/" + war_file, "/tmp/")

    with lcd('resources/public'):
        local("tar czf public.tar.gz *")

    put("resources/public/public.tar.gz", "/tmp/")

    run("rm -rf /var/www/astrocats/*")
    run("tar xvf /tmp/public.tar.gz -C /var/www/astrocats")

    with settings(sudo_user='jetty'):
        sudo("cp -f /tmp/" + war_file +
             " /usr/local/jetty/webapps/astrocats.war")

    sudo("service jetty restart")
    sudo("service nginx restart")

    local("rm -f resources/public/public.tar.gz")


