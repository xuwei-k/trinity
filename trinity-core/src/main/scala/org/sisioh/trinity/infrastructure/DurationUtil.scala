/*
 * Copyright 2010 TRICREO, Inc. (http://tricreo.jp/)
 * Copyright 2013 Sisioh Project and others. (http://sisioh.org/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.sisioh.trinity.infrastructure

import com.twitter.util.{Duration => TDuration}
import scala.concurrent.duration.{Duration => SDuration, _}
import java.util.concurrent.TimeUnit


object DurationUtil {

  implicit class TDurationToSDuration(val duration: TDuration) extends AnyVal {
    def toScala: SDuration = {
      SDuration(duration.inNanoseconds, TimeUnit.NANOSECONDS)
    }
  }


  implicit class SDurationToTDuration(val duration: SDuration) extends AnyVal {
    def toTwitter: TDuration = {
      TDuration(duration.length, duration.unit)
    }
  }


}
