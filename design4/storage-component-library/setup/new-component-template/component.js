/* eslint-disable */
module.exports = (componentName) => ({
    content: `import React from 'react';
import { ${componentName}Props } from './${componentName}.types';
import styles from './${componentName}.module.scss';

/**
 * ${componentName}
 */
const ${componentName} = (props: ${componentName}Props): JSX.Element => {
    return (
        <div className={styles.wrapper}>
           {props.title}
        </div>
    );
};

export default ${componentName};
`,
    extension: `.tsx`,
});
