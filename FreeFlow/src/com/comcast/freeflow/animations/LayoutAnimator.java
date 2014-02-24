/*******************************************************************************
 * Copyright 2013 Comcast Cable Communications Management, LLC
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
 ******************************************************************************/
package com.comcast.freeflow.animations;

import com.comcast.freeflow.core.Container;
import com.comcast.freeflow.core.LayoutChangeSet;

public abstract class LayoutAnimator {

	protected LayoutChangeSet changeSet;

	public LayoutAnimator() {

	}

	public LayoutChangeSet getChangeSet() {
		return changeSet;
	}

	public abstract void cancel();

	public abstract void animateChanges(LayoutChangeSet changes, Container callback);

	/*
	 * public abstract void transitionToFrame(final Frame of, final ItemProxy
	 * nf, final View v);
	 */

	public abstract void start();

}
