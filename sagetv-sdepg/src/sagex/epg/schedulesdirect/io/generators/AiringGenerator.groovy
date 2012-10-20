/*
*      Copyright 2011-2012 Battams, Derek
*
*       Licensed under the Apache License, Version 2.0 (the "License");
*       you may not use this file except in compliance with the License.
*       You may obtain a copy of the License at
*
*          http://www.apache.org/licenses/LICENSE-2.0
*
*       Unless required by applicable law or agreed to in writing, software
*       distributed under the License is distributed on an "AS IS" BASIS,
*       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*       See the License for the specific language governing permissions and
*       limitations under the License.
*/
package sagex.epg.schedulesdirect.io.generators

import org.apache.log4j.Logger

import sagex.epg.schedulesdirect.data.SageAiring

class AiringGenerator extends Generator {
	static private final Logger LOG = Logger.getLogger(AiringGenerator)

	AiringGenerator(def root) { super(root, 'air') }

	protected Collection<SageAiring> generate(String scriptName) {
		Binding binding = new Binding()
		File absPath = new File(root + File.separator + scriptName).getAbsoluteFile()
		binding.setVariable(Generator.LOG_VAR_NAME, getLogger(absPath.name.substring(0, absPath.name.lastIndexOf('.'))))
		try {
			return engine.run(scriptName, binding)
		} catch(ScriptException e) {
			LOG.error "Parsing of '$absPath' failed!", e
		} catch(RuntimeException e) {
			LOG.error "Runtime error with '$absPath'!", e
		}
		return null
	}

	SageAiring[] generate() {
		LOG.debug 'Airing generator processing has started!'
		def start = System.currentTimeMillis()
		resetLogger()
		files = new File(root).list(new FilenameFilter() {
			boolean accept(File f, String fName) {
				return fName.toLowerCase().endsWith(Generator.SCRIPT_EXTENSION)
			}
		})
		def airs = []
		files.each {
			def fStart = System.currentTimeMillis()
			def result = generate(it)
			if(LOG.isDebugEnabled())
				LOG.debug "Processed '$it' in ${System.currentTimeMillis() - fStart}ms"
			if(result != null && result instanceof Collection) {
				try {
					airs.addAll(result)
				} catch(Exception e) {
					LOG.error "Error processing return value of script '$it'; result ignored!", e
				}
			} else
				LOG.error "Script '$it' did not return a collection of SageAiring objects; result ignored!"
		}
		LOG.info "Processed airing generators in ${System.currentTimeMillis() - start}ms"
		return airs
	}
}
