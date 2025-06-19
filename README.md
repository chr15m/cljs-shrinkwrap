# CLJS Shrinkwrap

Tool to package ClojureScript `nbb` scripts into standalone Node.js binaries.

```shell
# install cljs-shrinkwrap
npm i chr15m/cljs-shrinkwrap
# turn myscript.cljs into a binary 'myscript' that only depends on Node
npx shrinkwrap -i myscript.cljs -o myscript
```

Shrinkwrap bundles your `.cljs` file and `nbb` and `node_modules` deps into a single standalone executable file which only relies on Node.js.

This allows you to distribute your ClojureScript command-line tools as single executable files that only require Node.js to run, without needing `nbb` or other ClojureScript tooling installed on the user's system.

The resulting binary can be run with any version of Node.js going back to v18.

## Usage

You can run `shrinkwrap` using `npx` if it's listed in your project's `package.json` dependencies, or directly if you've installed globally.

### Using npx (if installed as a dependency):

```shell
npx shrinkwrap -i your-script.cljs -o your-binary-name
```

This will create an executable file named `your-binary-name` in the current directory.

### Command-Line Options:

- `-i, --input FILE`: Specifies the input ClojureScript file (e.g., `your-script.cljs`).
- `-o, --output FILE`: Specifies the desired name for the output binary (e.g., `your-binary-name`).
- `-h, --help`: Displays help information.

## How It Works

The `shrinkwrap` tool performs the following steps:

1.  **Compile CLJS to JS**: It uses `nbb bundle` to compile your input ClojureScript file into an intermediate JavaScript module (`.mjs`).
2.  **Bundle with NCC**: It then uses `@vercel/ncc build` to bundle this intermediate JavaScript file and all its `node_modules` dependencies into a single, minified JavaScript file.
3.  **Create Executable**: Finally, it prepends a Node.js shebang (`#!/usr/bin/env -S node --experimental-default-type=module`) to the bundled JavaScript content and saves it as the specified output file, making it executable.

## Technology

- [nbb](https://github.com/babashka/nbb): Scripting with ClojureScript on Node.js.
- [@vercel/ncc](https://github.com/vercel/ncc): Compiles a Node.js module into a single file, together with all its dependencies, GCC-style.
- [clojure.tools.cli](https://github.com/clojure/tools.cli): For command-line argument parsing in Clojure.

## License

MIT
