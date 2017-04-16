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
