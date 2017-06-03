/* This source is used to easily generate assembly for
   the Grace Standard Library. The names putc, puts and getc
   are already defined inside headers, so putcc, putss and getcc
   are used instead. The actual assembly files contain the
   correct names (putc, puts, getc). You should use those
   pre-compiled files. If you recompile from here instead,
   you should manually change every name instance inside the
   generated assembly code */

#include <inttypes.h>
#include <stdio.h>
#include <string.h>

void _puti (int32_t n) {
    printf("%" PRId32, n);
}

void _putc (char c) {
    putchar(c);
}

void _puts(char string[]) {
    printf("%s", string);
}

int32_t _geti() {
    int32_t i;
    scanf("%" SCNd32, &i);
    return i;
}

char _getc() {
    char c;
    scanf(" %c", &c);
    return c;
}

void _gets(int32_t n, char s[]) {
    fgets(s, n, stdin);
    size_t len = strlen(s);
    if (s[len-1] == '\n') {
        s[len-1] = '\0';
    }
}

int32_t _abs (int32_t n) {
    return (n >= 0 ? n : n*-1);
}

int32_t _ord (char c) {
    return (int32_t)c;
}

char _chr(int32_t i) {
    return (char)i;
}
