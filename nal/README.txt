Example experience files are in directory Examples. 

- "test??" contains individual unit tests, generally organized by NAL level

- "Example-NALn-*.txt" contains single step examples for most of the inference rules defined in NAL level n. The "edited" version contains English translations, and with the unrelated information removed; the "unedited" version contains the actual input/output data recorded by the "Save Experience" function of the GUI. The files can be loaded using the "Load Experience" function of the GUI. 

- "Example-MultiStep-edited" contains multi-step inference examples described in http://code.google.com/p/open-nars/wiki/MultiStepExamples

- "Example-NLP-edited" contains an example of natural language processing described in the AGI-13 paper "Natural Language Processing by Reasoning and Learning".






These scripts are individual unit tests for verifying correct and 
predictable operation with the original default NAR parameters.


Lines beginning with:

  [number]: # of cycles to process before continuing to next line

  ' (apostrophe): comments; not interpreted, but instead are re-created as ECHO channel output

  '' (apostrophe): embedded Javascript code evaluated during testing.  Examples:

        ''print(out)
            out is a reference to the current output buffer, containing a list of strings; one for each output

        ''outputMustContain('...')
            used for ensuring that the output buffer so far has a line containing the parameter string
            
            


