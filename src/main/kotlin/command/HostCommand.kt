package command

import Buffer
import command.base.NoArgCommand
import conf.HostConfig
import d2r.CommandMessageType
import extension.arg0
import extension.CoroutineExtensions.launch
import extension.log
import extension.type
import external.cors.cors
import external.express.express
import external.ws.WebSocket
import external.ws.WebSocketServer
import external.ws.WebSocketServerOptions
import http.IncomingMessage

object HostCommand : NoArgCommand("host") {

  override suspend fun handle() {
    val gameName = "${HostConfig.get("game:prefix")}${HostConfig.get("game:counter")}"
    log("game name: $gameName")
    IPCommand.handle()

    val wsPort = HostConfig.get("port").toInt()
    val wssOptions = WebSocketServerOptions(port = wsPort)
    val wss = WebSocketServer(wssOptions)
    wss
      .on("connection") { socket: WebSocket, _: IncomingMessage ->
        log("client connected")
        log("client count: ${wss.clients.size}")
        socket.on("message") { msg: Buffer, _ ->
          val command = msg.toString()
          println("received $command")
          when (command.type()) {
            CommandMessageType.CLIENT_REG -> {
              val clientName = command.arg0()
              println("client:${clientName}")
              socket.asDynamic().clientName = clientName
            }
          }
        }
        socket.send("${CommandMessageType.GRETTING}|hello ology client, current game is $gameName")

      }


    val httpPort = wsPort + 1
    express()
      .apply {
        use(cors())
        get("/ng") { _, res ->
          launch {
            NgCommand.handle()
            val gamePrefix = HostConfig.get("game:prefix")
            val counter = HostConfig.get("game:counter")
            val pwd = HostConfig.get("game:pwd")
            val gamePayload = "$gamePrefix$counter|$pwd"
            wss.clients.forEach({ client, _, _ ->
              client.send("${CommandMessageType.NEXT_GAME}|$gamePayload")
            })
            res.status = 200
            res.send(gamePayload)
          }
        }

        get("/clientAction/:action") { req, res ->
          wss.clients.forEach({ client, _, _ ->
            client.send("${CommandMessageType.DO_ACTION}|${req.params.action}")
          })
          res.status = 200
          res.send("ok")
        }
      }
      .listen(port = httpPort) {
        log("http server started localhost:$httpPort")
      }
  }
}
