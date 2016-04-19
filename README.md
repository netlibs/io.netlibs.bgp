# Java BGPv4 Library

This project contains a fork of BGP4J (https://github.com/rbieniek/BGP4J) ported to modern version of netty.

In addition to moving to a modern version of netty, there is considerably refactoring to seperate the server and client to create a "core" BGP library, and some initial work to add flow control to avoid overloading ourselves by reading faster than we can process.

It also adds multiple NLRI support and support for MPLS, and initial seperation of RIB and the transport/session management.

