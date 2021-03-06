package org.sisioh.trinity.example

import org.jboss.netty.handler.codec.http.HttpResponseStatus
import org.sisioh.trinity.domain.controller.SimpleController
import org.sisioh.trinity.domain.http.ContentType
import org.sisioh.trinity.view.freemarker.FreeMarkerRenderer
import org.sisioh.trinity.view.scalate.ScalateRenderer
import org.sisioh.trinity.view.thymeleaf.ThymeleafRenderer
import org.sisioh.trinity.view.velocity.VelocityRenderer

object ScalatraLikeExample extends App with ApplicationContext {

  object ScalatraLikeController extends SimpleController {

    /**
     * Basic Example
     *
     * curl http://localhost:7070/hello => "hello world"
     */
    post("/") {
      request =>
        val r = request.request.getContentString()
        responseBuilder.withPlain("r = "+r).toFinagleResponseFuture
    }

    /**
     * Route parameters
     *
     * curl http://localhost:7070/user/dave => "hello dave"
     */
    get("/user/:username") {
      request =>
        val username = request.routeParams.getOrElse("username", "default_user")
        responseBuilder.withPlain("hello " + username).toFinagleResponseFuture
    }

    /**
     * Setting Headers
     *
     * curl -I http://localhost:7070/headers => "Foo:Bar"
     */
    get("/headers") {
      request =>
        responseBuilder.withPlain("look at headers").withHeader("Foo", "Bar").toFinagleResponseFuture
    }

    /**
     * Rendering json
     *
     * curl -I http://localhost:7070/headers => "Foo:Bar"
     */
    get("/data.json") {
      request =>
        import org.json4s.JsonDSL._
        responseBuilder.withJson(Map("foo" -> "bar")).toFinagleResponseFuture
    }

    /**
     * Query params
     *
     * curl http://localhost:7070/search?q=foo => "no results for foo"
     */
    get("/search") {
      request =>
        request.params.get("q") match {
          case Some(q) => responseBuilder.withPlain("no results for " + q).toFinagleResponseFuture
          case None => responseBuilder.withPlain("query param q needed").withStatus(HttpResponseStatus.valueOf(500)).toFinagleResponseFuture
        }
    }

    /**
     * Uploading files
     *
     * curl -F avatar=@/path/to/img http://localhost:7070/profile
     */
    post("/profile") {
      request =>
        request.multiParams.get("avatar").map {
          avatar =>
            println("content type is " + avatar.contentType)
            avatar.writeToFile("/tmp/avatar") //writes uploaded avatar to /tmp/avatar
        }
        responseBuilder.withPlain("ok").toFinagleResponseFuture
    }


    get("/template1") {
      request =>
        responseBuilder.withBodyRenderer(ScalateRenderer("scalate.mustache", Map("message" -> "hello"))).toFinagleResponseFuture
    }

    get("/template2") {
      request =>
        responseBuilder.withBodyRenderer(ThymeleafRenderer("thymeleaf", Map("message" -> "hello"))).toFinagleResponseFuture
    }

    get("/template3") {
      request =>
        responseBuilder.withBodyRenderer(VelocityRenderer("velocity.vm", Map("message" -> "hello"))).toFinagleResponseFuture
    }

    get("/template4") {
      request =>
        responseBuilder.withBodyRenderer(FreeMarkerRenderer("freemarker.tpl", Map("message" -> "hello"))).toFinagleResponseFuture
    }

    /**
     * Custom Error Handling
     *
     * curl http://localhost:7070/error
     */
    get("/error") {
      request =>
        1234 / 0
        responseBuilder.withPlain("we never make it here").toFinagleResponseFuture
    }


    /**
     * Custom Error Handling with custom Exception
     *
     * curl http://localhost:7070/unautorized
     */
    get("/unauthorized") {
      request =>
        throw new UnauthorizedException
    }


    /**
     * Dispatch based on Content-Type
     *
     * curl http://localhost:7070/index.json
     * curl http://localhost:7070/index.html
     */
    get("/blog/index.:format") {
      request =>
        import org.json4s.JsonDSL._
        respondTo(request) {
          case ContentType.TextHtml => responseBuilder.withHtml("<h1>Hello</h1>").toFinagleResponseFuture
          case ContentType.AppJson => responseBuilder.withJson(Map("value" -> "hello")).toFinagleResponseFuture
        }
    }

    /**
     * Also works without :format route using browser Accept header
     *
     * curl -H "Accept: text/html" http://localhost:7070/another/page
     * curl -H "Accept: application/json" http://localhost:7070/another/page
     * curl -H "Accept: foo/bar" http://localhost:7070/another/page
     */
    get("/another/page") {
      request =>
        respondTo(request) {
          case ContentType.TextHtml => responseBuilder.withPlain("an html response").toFinagleResponseFuture
          case ContentType.AppJson => responseBuilder.withPlain("an json response").toFinagleResponseFuture
          case ContentType.All => responseBuilder.withPlain("default fallback response").toFinagleResponseFuture
        }
    }

    /**
     * Metrics are supported out of the box via Twitter's Ostrich library.
     * More details here: https://github.com/twitter/ostrich
     *
     * curl http://localhost:7070/slow_thing
     *
     * By default a stats server is started on 9990:
     *
     * curl http://localhost:9990/stats.txt
     *
     */
    get("/slow_thing") {
      request =>
        stats.counter("slow_thing").incr()
        stats.time("slow_thing time") {
          Thread.sleep(100)
        }
        responseBuilder.withPlain("slow").toFinagleResponseFuture
    }
  }

  application.registerController(ScalatraLikeController)
  application.start()

}
