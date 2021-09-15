# Storage Component Library

**storage-component-library** is a React Library which contains a collection of custom reusable React Components, documented 
with storybook and tested using Jest & React-Testing-Library. The intention is not to re-create the wheel, hence most of 
the React Component are created using third party libraries and stylesheets.

- [Usage](#usage)
- [Development](#development)
    - [Getting Started](#getting-started)
        - [Using Storybook](#using-storybook)
        - [Testing Library in another React App](#testing-library-in-another-react-application)
    - [Creating New Components](#creating-new-components)
    - [Adding Dependencies](#adding-dependencies)
    - [Calling External API](#calling-external-api)
    - [Writing Test Cases](#writing-test-cases)
- [Build and Publish](#build-and-publish)
    - [Build the library](#build-the-library)
    - [Publish the library](#publish-the-library)
    - [Additional Notes](#additional-notes)
        - [Private NPM Registry](#configuration-for-private-npm-optional)
        - [Semantic Versioning](#semantic-versioning)    

## Technology Stack

This library is designed using the following technology decisions:

- **React**
- **Typescript** (superset of JavaScript)
- **Eslint & Prettier**
- **Rollup** (javascript module bundler) for bundling the library and publishing to npm.
- **Storybook** (for documenting & developing/testing UI components in isolation).
- **Jest & React-Testing-Library** (for testing).
- **CSS Modules & SASS** (for styling)

## Usage

To use `storage-component-library` in your project, do the following:

1. Adding Library as dependency
    - `yarn add storage-component-library axios moment`
2. Import components
    - `import { TestComponent } from 'storage-component-library';`
3. Import Stylesheets if required. Check storybook for the instructions.
    - `import 'storage-component-library/dist/styles/whatever/styles.css>';`

## Development

### Getting Started

#### Using Storybook

1. Install the packages
    - `yarn install --ignore-scripts` -- **IMPORTANT** to use this command to prevent `postinstall` script from running.
2. Run storybook as a playground for developing
    - `yarn run storybook`
3. Run Test
    - `yarn run test`  

#### Testing Library in another React Application

Instead of publishing to NPM to test the library, you can follow these steps to test the library locally before publishing.

1. create a project using `npx create-react-app example`
2. Inside `storage-component-library` folder
    - use `yarn link` to create a link to the library
    - build the library `yarn run build`
3. Inside `example` project folder
    - run `yarn link storage-component-library` -- to link the library to example
    - inside `app.js`, import the component (eg. `import { TestComponent } from 'storage-component-library';`) and add the component (`<TestComponent />`)
    - start the application `yarn start`
4. You should receive the following error 
    ```
    Error: Invalid hook call. Hooks can only be called inside of the body of a function component. This could happen for one of the following reasons:
    1. You might have mismatching versions of React and the renderer (such as React DOM)
    2. You might be breaking the Rules of Hooks
    3. You might have more than one copy of React in the same app
    See https://fb.me/react-invalid-hook-call for tips about how to debug and fix this problem.
    ```
    - **Solution:** This is because the library is using a different version of React from your existing project. To resolve, follow the steps:
        1. In your project (`example`)
            - `cd node_modules/react && yarn link`
            - `cd node_modules/react-dom && yarn link`
        2. In your library (`storage-component-library`)
            - `yarn link react`
            - `yarn link react-dom` 
            - `yarn run build`
        3. In your project (`example`)
            - `yarn start`
5. Happy testing

### Creating New Components

To add a new reusable component to the library, follow the steps below:

```bash
# Create Component. Note: ComponentName should be CamelCase
yarn run create-new-component <ComponentName>
```

### Adding Dependencies

Note that when adding or removing dependencies, always use `--ignore-scripts` to prevent `postinstall` script in package.json
from running. Example below:

```bash
yarn add PACKAGE_NAME --ignore-scripts
yarn remove PACKAGE_NAME --ignore-scripts
```

### Calling External API

When creating stories, you can call a mock api for testing your component. Refer to how `TestComponent` does it. Below is
the list of files you would want to look at:
- [middleware.js](.storybook/middleware.js)
- [TestComponent.tsx](src/components/TestComponent/TestComponent.tsx)
- [TestComponent.stories.tsx](src/components/TestComponent/TestComponent.stories.tsx)

### Writing Test Cases

Refer to [React Testing Library](https://testing-library.com/docs/react-testing-library/intro/) and [Jest](https://jestjs.io/)
for more details on how to write test cases. 

```bash
yarn run test
yarn run test:watch
```

## Build and Publish 

### Build the Library

Verify the following:
1. **Components are exported correctly**
    - [base.ts](src/index.ts)
2. **Third Party Library have been added to build directory**
    - You can expose third party library stylesheet. Refer to [rollup.config.js](rollup.config.js) for an example

Run the commands below to build the library:

```bash
yarn run build
```

### Publish the library

```bash
# Login to NPM
yarn login

# Publish tp NPM
yarn publish
```

### Additional Notes

#### Configuration for private npm (optional)

- Refer to [package.json](package.json) for the publishing config
    - add the following script to build the library before publishing
        - `"prepublishOnly": "yarn run build"`
    - point publishing registry to npm private repository
        ```
        "publishConfig": {
            "registry": <LINK TO PRIVATE REPOSITORY>
        }
        ```
- To get logic credentials for CI
    - Add the following config to `.npmrc` file
        - `npm config set _auth ${AUTH_TOKEN}`
        - `npm config set email ${EMAIL}`
        - `npm config set user ${USER}`
        - `npm config set always_auth true`
        
#### Semantic Versioning

Follow these guidelines when changing the versions:

- First Release (**New Product**) -- Start with 1.0.0
- Bug Fixes / Feature Updates (**Patch Release**) -- Increment the third digit (eg. 1.0.1)
- New Feature (**Minor Release**) -- Increment the middle digit and reset the last digit to zero (eg. 1.2.0)
- Changes that break backward compatibility (**Major Release**) -- Increment the first digit and reset the middle & last digit to zero (eg. 2.0.0)
