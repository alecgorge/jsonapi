package com.alecgorge.minecraft.jsonapi.packets.netty.router;

/*
 * Copyright (c) 2011-2013 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

/**
 * A generic event handler
 * <p>
 * 
 * This interface is used heavily throughout vert.x as a handler for all types
 * of asynchronous occurrences.
 * <p>
 * 
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="http://alecgorge.com">Alec Gorge</a>
 */
public interface Handler<R, E> {

	/**
	 * Something has happened, so handle it.
	 */
	R handle(E event);
}
