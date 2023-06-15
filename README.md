bladenightapp-replay
====================

Simulates bladenight clients for testing purposes.
added key for basic auth with server

--speed 20 --count 30 --startperiod=5000  --url wss://localhost:8081/ws --key test:test

--file bladenight-log-2013.log --fromtime "2013-06-17T21:00" --totime "2013-06-17T23:15" --timelapse 10 --url wss://localhost:8081/ws

--file 2015/protocol-filtered.log --fromtime "2015-05-11T21:10" --totime "2015-05-11T23:15"  --url wss://localhost:8081/ws

--file e5a18b328c3dfde5fac2.log --fromtime "2013-06-17T21:00" --totime "2013-06-17T23:15" --timelapse 10 --url wss://localhost:8081/ws

--file protocol-2014-anonym.log --timelapse 1200 --events-dir 2014/events --routes-dir 2014/routes"

--file 2015/protocol-filtered.log --timelapse 1000  --events-dir 2015/events --routes-dir 2015/routes"

Config for Server v2

-startperiod=300 --url wss://host.de:8081/ws --key user:key

LICENSE

This software is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this software.  If not, see [http://www.gnu.org/licenses/](http://www.gnu.org/licenses/).
