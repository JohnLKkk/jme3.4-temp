/*
 * Copyright (c) 2009-2021 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package jme3test.light.pbr;

import com.jme3.environment.generation.JobProgressAdapter;
import com.jme3.light.LightProbe;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A basic logger for environment map rendering progress.
 * @author nehon
 */
public class ConsoleProgressReporter extends JobProgressAdapter<LightProbe>{

    private static final Logger logger = Logger.getLogger(ConsoleProgressReporter.class.getName());
    
    private long time;

    @Override
    public void start() {
        time = System.currentTimeMillis();
        logger.log(Level.INFO,"Starting generation");
    }

    @Override
    public void progress(double value) {       
        logger.log(Level.INFO, "Progress : {0}%", (value * 100));
    }

    @Override
    public void step(String message) {       
        logger.info(message);
    }
    
    @Override
    public void done(LightProbe result) {
        long end = System.currentTimeMillis();
        logger.log(Level.INFO, "Generation done in {0}", (end - time) / 1000f);
    }
    
}
