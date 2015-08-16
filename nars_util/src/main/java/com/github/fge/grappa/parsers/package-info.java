/*
 * Copyright (C) 2014 Francis Galiegue <fgaliegue@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Event-based parser
 *
 * <p>The base parser class ({@link
 * com.github.fge.grappa.parsers.ListeningParser}) uses Guava's {@link
 * com.google.common.eventbus.EventBus} to dispatch events.</p>
 *
 * <p>The choice of this class over other implementations is performance;
 * <a href="https://github.com/bennidi/mbassador"
 * target="_blank">mbassador</a> was also considered, as it claimed to perform
 * better, but tests done in the <a
 * href="https://github.com/fge/grappa-support" target="_blank">support
 * project</a> proved otherwise: Guava's {@code EventBus} is three times as
 * fast for the needs of this package. And speed matters here :)</p>
 */
package com.github.fge.grappa.parsers;
