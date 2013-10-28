#!/usr/bin/python

# Simple HTTP server

import BaseHTTPServer
import CGIHTTPServer

handler = CGIHTTPServer.CGIHTTPRequestHandler
handler.cgi_directories = ["/"]

BaseHTTPServer.HTTPServer(('', 8000), handler).serve_forever()
