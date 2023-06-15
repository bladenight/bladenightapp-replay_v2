#!/usr/bin/env bash

scp bna:/home/mbnapp/bladenightserver/config/productive/logs/protocol.log . || exit 1
rsync -axv --delete -e ssh bna:/home/mbnapp/bladenightserver/config/productive/routes/ ~/Desktop/workspace_demo3/bladenightapp-replay/2015/routes/
rsync -axv --delete -e ssh bna:/home/mbnapp/bladenightserver/config/productive/events/ ~/Desktop/workspace_demo3/bladenightapp-replay/2015/events/

