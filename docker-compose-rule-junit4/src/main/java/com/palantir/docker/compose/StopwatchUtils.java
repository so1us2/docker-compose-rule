/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.docker.compose;

import com.google.common.base.Stopwatch;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

class StopwatchUtils {
    private StopwatchUtils() { }

    static Duration time(CheckedRunnable runnable) throws InterruptedException, IOException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            runnable.run();
        } finally {
            stopwatch.stop();
        }
        return toDuration(stopwatch);
    }

    static Duration toDuration(Stopwatch stopwatch) {
        return Duration.ofMillis(stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }

    interface CheckedRunnable {
        void run() throws InterruptedException, IOException;
    }
}