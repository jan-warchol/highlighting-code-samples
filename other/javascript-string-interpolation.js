const brackets = [
  { open: 'cat', close: 'tac'},
  { open: 'cAt', close: 'tAc'},
  { open: 'CAT', close: 'TAC'}
];

const validatorRegexp = new RegExp(`(${
  brackets
    .reduce((acc, {open, close}) => acc.concat([open, close]), [])
    .join('|')
})`, 'g');

const validateInput = (string) => string.replace(validatorRegexp, '') === '';

