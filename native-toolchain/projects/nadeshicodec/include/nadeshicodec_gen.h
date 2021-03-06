#pragma once
#define QUAD_LEN_PER_CHAR 3
#define QUAD(i) (&QUADS[(QUAD_LEN_PER_CHAR+1)*(i)])
const uint8_t QUADS[] = {
  0x20, 0x00, 0x00, 0x00,
  0xe2, 0xa2, 0x80, 0x00,
  0xe2, 0xa1, 0x80, 0x00,
  0xe2, 0xa3, 0x80, 0x00,
  0xe2, 0xa0, 0xa0, 0x00,
  0xe2, 0xa2, 0xa0, 0x00,
  0xe2, 0xa1, 0xa0, 0x00,
  0xe2, 0xa3, 0xa0, 0x00,
  0xe2, 0xa0, 0x84, 0x00,
  0xe2, 0xa2, 0x84, 0x00,
  0xe2, 0xa1, 0x84, 0x00,
  0xe2, 0xa3, 0x84, 0x00,
  0xe2, 0xa0, 0xa4, 0x00,
  0xe2, 0xa2, 0xa4, 0x00,
  0xe2, 0xa1, 0xa4, 0x00,
  0xe2, 0xa3, 0xa4, 0x00,
  0xe2, 0xa0, 0x90, 0x00,
  0xe2, 0xa2, 0x90, 0x00,
  0xe2, 0xa1, 0x90, 0x00,
  0xe2, 0xa3, 0x90, 0x00,
  0xe2, 0xa0, 0xb0, 0x00,
  0xe2, 0xa2, 0xb0, 0x00,
  0xe2, 0xa1, 0xb0, 0x00,
  0xe2, 0xa3, 0xb0, 0x00,
  0xe2, 0xa0, 0x94, 0x00,
  0xe2, 0xa2, 0x94, 0x00,
  0xe2, 0xa1, 0x94, 0x00,
  0xe2, 0xa3, 0x94, 0x00,
  0xe2, 0xa0, 0xb4, 0x00,
  0xe2, 0xa2, 0xb4, 0x00,
  0xe2, 0xa1, 0xb4, 0x00,
  0xe2, 0xa3, 0xb4, 0x00,
  0xe2, 0xa0, 0x82, 0x00,
  0xe2, 0xa2, 0x82, 0x00,
  0xe2, 0xa1, 0x82, 0x00,
  0xe2, 0xa3, 0x82, 0x00,
  0xe2, 0xa0, 0xa2, 0x00,
  0xe2, 0xa2, 0xa2, 0x00,
  0xe2, 0xa1, 0xa2, 0x00,
  0xe2, 0xa3, 0xa2, 0x00,
  0xe2, 0xa0, 0x86, 0x00,
  0xe2, 0xa2, 0x86, 0x00,
  0xe2, 0xa1, 0x86, 0x00,
  0xe2, 0xa3, 0x86, 0x00,
  0xe2, 0xa0, 0xa6, 0x00,
  0xe2, 0xa2, 0xa6, 0x00,
  0xe2, 0xa1, 0xa6, 0x00,
  0xe2, 0xa3, 0xa6, 0x00,
  0xe2, 0xa0, 0x92, 0x00,
  0xe2, 0xa2, 0x92, 0x00,
  0xe2, 0xa1, 0x92, 0x00,
  0xe2, 0xa3, 0x92, 0x00,
  0xe2, 0xa0, 0xb2, 0x00,
  0xe2, 0xa2, 0xb2, 0x00,
  0xe2, 0xa1, 0xb2, 0x00,
  0xe2, 0xa3, 0xb2, 0x00,
  0xe2, 0xa0, 0x96, 0x00,
  0xe2, 0xa2, 0x96, 0x00,
  0xe2, 0xa1, 0x96, 0x00,
  0xe2, 0xa3, 0x96, 0x00,
  0xe2, 0xa0, 0xb6, 0x00,
  0xe2, 0xa2, 0xb6, 0x00,
  0xe2, 0xa1, 0xb6, 0x00,
  0xe2, 0xa3, 0xb6, 0x00,
  0xe2, 0xa0, 0x88, 0x00,
  0xe2, 0xa2, 0x88, 0x00,
  0xe2, 0xa1, 0x88, 0x00,
  0xe2, 0xa3, 0x88, 0x00,
  0xe2, 0xa0, 0xa8, 0x00,
  0xe2, 0xa2, 0xa8, 0x00,
  0xe2, 0xa1, 0xa8, 0x00,
  0xe2, 0xa3, 0xa8, 0x00,
  0xe2, 0xa0, 0x8c, 0x00,
  0xe2, 0xa2, 0x8c, 0x00,
  0xe2, 0xa1, 0x8c, 0x00,
  0xe2, 0xa3, 0x8c, 0x00,
  0xe2, 0xa0, 0xac, 0x00,
  0xe2, 0xa2, 0xac, 0x00,
  0xe2, 0xa1, 0xac, 0x00,
  0xe2, 0xa3, 0xac, 0x00,
  0xe2, 0xa0, 0x98, 0x00,
  0xe2, 0xa2, 0x98, 0x00,
  0xe2, 0xa1, 0x98, 0x00,
  0xe2, 0xa3, 0x98, 0x00,
  0xe2, 0xa0, 0xb8, 0x00,
  0xe2, 0xa2, 0xb8, 0x00,
  0xe2, 0xa1, 0xb8, 0x00,
  0xe2, 0xa3, 0xb8, 0x00,
  0xe2, 0xa0, 0x9c, 0x00,
  0xe2, 0xa2, 0x9c, 0x00,
  0xe2, 0xa1, 0x9c, 0x00,
  0xe2, 0xa3, 0x9c, 0x00,
  0xe2, 0xa0, 0xbc, 0x00,
  0xe2, 0xa2, 0xbc, 0x00,
  0xe2, 0xa1, 0xbc, 0x00,
  0xe2, 0xa3, 0xbc, 0x00,
  0xe2, 0xa0, 0x8a, 0x00,
  0xe2, 0xa2, 0x8a, 0x00,
  0xe2, 0xa1, 0x8a, 0x00,
  0xe2, 0xa3, 0x8a, 0x00,
  0xe2, 0xa0, 0xaa, 0x00,
  0xe2, 0xa2, 0xaa, 0x00,
  0xe2, 0xa1, 0xaa, 0x00,
  0xe2, 0xa3, 0xaa, 0x00,
  0xe2, 0xa0, 0x8e, 0x00,
  0xe2, 0xa2, 0x8e, 0x00,
  0xe2, 0xa1, 0x8e, 0x00,
  0xe2, 0xa3, 0x8e, 0x00,
  0xe2, 0xa0, 0xae, 0x00,
  0xe2, 0xa2, 0xae, 0x00,
  0xe2, 0xa1, 0xae, 0x00,
  0xe2, 0xa3, 0xae, 0x00,
  0xe2, 0xa0, 0x9a, 0x00,
  0xe2, 0xa2, 0x9a, 0x00,
  0xe2, 0xa1, 0x9a, 0x00,
  0xe2, 0xa3, 0x9a, 0x00,
  0xe2, 0xa0, 0xba, 0x00,
  0xe2, 0xa2, 0xba, 0x00,
  0xe2, 0xa1, 0xba, 0x00,
  0xe2, 0xa3, 0xba, 0x00,
  0xe2, 0xa0, 0x9e, 0x00,
  0xe2, 0xa2, 0x9e, 0x00,
  0xe2, 0xa1, 0x9e, 0x00,
  0xe2, 0xa3, 0x9e, 0x00,
  0xe2, 0xa0, 0xbe, 0x00,
  0xe2, 0xa2, 0xbe, 0x00,
  0xe2, 0xa1, 0xbe, 0x00,
  0xe2, 0xa3, 0xbe, 0x00,
  0xe2, 0xa0, 0x81, 0x00,
  0xe2, 0xa2, 0x81, 0x00,
  0xe2, 0xa1, 0x81, 0x00,
  0xe2, 0xa3, 0x81, 0x00,
  0xe2, 0xa0, 0xa1, 0x00,
  0xe2, 0xa2, 0xa1, 0x00,
  0xe2, 0xa1, 0xa1, 0x00,
  0xe2, 0xa3, 0xa1, 0x00,
  0xe2, 0xa0, 0x85, 0x00,
  0xe2, 0xa2, 0x85, 0x00,
  0xe2, 0xa1, 0x85, 0x00,
  0xe2, 0xa3, 0x85, 0x00,
  0xe2, 0xa0, 0xa5, 0x00,
  0xe2, 0xa2, 0xa5, 0x00,
  0xe2, 0xa1, 0xa5, 0x00,
  0xe2, 0xa3, 0xa5, 0x00,
  0xe2, 0xa0, 0x91, 0x00,
  0xe2, 0xa2, 0x91, 0x00,
  0xe2, 0xa1, 0x91, 0x00,
  0xe2, 0xa3, 0x91, 0x00,
  0xe2, 0xa0, 0xb1, 0x00,
  0xe2, 0xa2, 0xb1, 0x00,
  0xe2, 0xa1, 0xb1, 0x00,
  0xe2, 0xa3, 0xb1, 0x00,
  0xe2, 0xa0, 0x95, 0x00,
  0xe2, 0xa2, 0x95, 0x00,
  0xe2, 0xa1, 0x95, 0x00,
  0xe2, 0xa3, 0x95, 0x00,
  0xe2, 0xa0, 0xb5, 0x00,
  0xe2, 0xa2, 0xb5, 0x00,
  0xe2, 0xa1, 0xb5, 0x00,
  0xe2, 0xa3, 0xb5, 0x00,
  0xe2, 0xa0, 0x83, 0x00,
  0xe2, 0xa2, 0x83, 0x00,
  0xe2, 0xa1, 0x83, 0x00,
  0xe2, 0xa3, 0x83, 0x00,
  0xe2, 0xa0, 0xa3, 0x00,
  0xe2, 0xa2, 0xa3, 0x00,
  0xe2, 0xa1, 0xa3, 0x00,
  0xe2, 0xa3, 0xa3, 0x00,
  0xe2, 0xa0, 0x87, 0x00,
  0xe2, 0xa2, 0x87, 0x00,
  0xe2, 0xa1, 0x87, 0x00,
  0xe2, 0xa3, 0x87, 0x00,
  0xe2, 0xa0, 0xa7, 0x00,
  0xe2, 0xa2, 0xa7, 0x00,
  0xe2, 0xa1, 0xa7, 0x00,
  0xe2, 0xa3, 0xa7, 0x00,
  0xe2, 0xa0, 0x93, 0x00,
  0xe2, 0xa2, 0x93, 0x00,
  0xe2, 0xa1, 0x93, 0x00,
  0xe2, 0xa3, 0x93, 0x00,
  0xe2, 0xa0, 0xb3, 0x00,
  0xe2, 0xa2, 0xb3, 0x00,
  0xe2, 0xa1, 0xb3, 0x00,
  0xe2, 0xa3, 0xb3, 0x00,
  0xe2, 0xa0, 0x97, 0x00,
  0xe2, 0xa2, 0x97, 0x00,
  0xe2, 0xa1, 0x97, 0x00,
  0xe2, 0xa3, 0x97, 0x00,
  0xe2, 0xa0, 0xb7, 0x00,
  0xe2, 0xa2, 0xb7, 0x00,
  0xe2, 0xa1, 0xb7, 0x00,
  0xe2, 0xa3, 0xb7, 0x00,
  0xe2, 0xa0, 0x89, 0x00,
  0xe2, 0xa2, 0x89, 0x00,
  0xe2, 0xa1, 0x89, 0x00,
  0xe2, 0xa3, 0x89, 0x00,
  0xe2, 0xa0, 0xa9, 0x00,
  0xe2, 0xa2, 0xa9, 0x00,
  0xe2, 0xa1, 0xa9, 0x00,
  0xe2, 0xa3, 0xa9, 0x00,
  0xe2, 0xa0, 0x8d, 0x00,
  0xe2, 0xa2, 0x8d, 0x00,
  0xe2, 0xa1, 0x8d, 0x00,
  0xe2, 0xa3, 0x8d, 0x00,
  0xe2, 0xa0, 0xad, 0x00,
  0xe2, 0xa2, 0xad, 0x00,
  0xe2, 0xa1, 0xad, 0x00,
  0xe2, 0xa3, 0xad, 0x00,
  0xe2, 0xa0, 0x99, 0x00,
  0xe2, 0xa2, 0x99, 0x00,
  0xe2, 0xa1, 0x99, 0x00,
  0xe2, 0xa3, 0x99, 0x00,
  0xe2, 0xa0, 0xb9, 0x00,
  0xe2, 0xa2, 0xb9, 0x00,
  0xe2, 0xa1, 0xb9, 0x00,
  0xe2, 0xa3, 0xb9, 0x00,
  0xe2, 0xa0, 0x9d, 0x00,
  0xe2, 0xa2, 0x9d, 0x00,
  0xe2, 0xa1, 0x9d, 0x00,
  0xe2, 0xa3, 0x9d, 0x00,
  0xe2, 0xa0, 0xbd, 0x00,
  0xe2, 0xa2, 0xbd, 0x00,
  0xe2, 0xa1, 0xbd, 0x00,
  0xe2, 0xa3, 0xbd, 0x00,
  0xe2, 0xa0, 0x8b, 0x00,
  0xe2, 0xa2, 0x8b, 0x00,
  0xe2, 0xa1, 0x8b, 0x00,
  0xe2, 0xa3, 0x8b, 0x00,
  0xe2, 0xa0, 0xab, 0x00,
  0xe2, 0xa2, 0xab, 0x00,
  0xe2, 0xa1, 0xab, 0x00,
  0xe2, 0xa3, 0xab, 0x00,
  0xe2, 0xa0, 0x8f, 0x00,
  0xe2, 0xa2, 0x8f, 0x00,
  0xe2, 0xa1, 0x8f, 0x00,
  0xe2, 0xa3, 0x8f, 0x00,
  0xe2, 0xa0, 0xaf, 0x00,
  0xe2, 0xa2, 0xaf, 0x00,
  0xe2, 0xa1, 0xaf, 0x00,
  0xe2, 0xa3, 0xaf, 0x00,
  0xe2, 0xa0, 0x9b, 0x00,
  0xe2, 0xa2, 0x9b, 0x00,
  0xe2, 0xa1, 0x9b, 0x00,
  0xe2, 0xa3, 0x9b, 0x00,
  0xe2, 0xa0, 0xbb, 0x00,
  0xe2, 0xa2, 0xbb, 0x00,
  0xe2, 0xa1, 0xbb, 0x00,
  0xe2, 0xa3, 0xbb, 0x00,
  0xe2, 0xa0, 0x9f, 0x00,
  0xe2, 0xa2, 0x9f, 0x00,
  0xe2, 0xa1, 0x9f, 0x00,
  0xe2, 0xa3, 0x9f, 0x00,
  0xe2, 0xa0, 0xbf, 0x00,
  0xe2, 0xa2, 0xbf, 0x00,
  0xe2, 0xa1, 0xbf, 0x00,
  0xe2, 0xa3, 0xbf, 0x00
};
const uint8_t STR0[] = {
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20,
  0x20, 0x00
};
#define STR0_AT(i) (&STR0[(159-(i))*1])
const uint8_t STR1[] = {
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf,
  0xe2, 0xa3, 0xbf, 0x00
};
#define STR1_AT(i) (&STR1[(159-(i))*3])
#define STR(i,chr) ((chr)==1?STR1_AT(i):STR0_AT(i))
const int PALETTE[] = {
  0x000000, 0x0f0f0f, 0x1e1e1e, 0x2d2d2d, 0x3c3c3c, 0x4b4b4b, 0x5a5a5a, 0x696969,
  0x787878, 0x878787, 0x969696, 0xa5a5a5, 0xb4b4b4, 0xc3c3c3, 0xd2d2d2, 0xe1e1e1,
  0x000000, 0x00003f, 0x00007f, 0x0000bf, 0x0000ff, 0x002400, 0x00243f, 0x00247f,
  0x0024bf, 0x0024ff, 0x004800, 0x00483f, 0x00487f, 0x0048bf, 0x0048ff, 0x006d00,
  0x006d3f, 0x006d7f, 0x006dbf, 0x006dff, 0x009100, 0x00913f, 0x00917f, 0x0091bf,
  0x0091ff, 0x00b600, 0x00b63f, 0x00b67f, 0x00b6bf, 0x00b6ff, 0x00da00, 0x00da3f,
  0x00da7f, 0x00dabf, 0x00daff, 0x00ff00, 0x00ff3f, 0x00ff7f, 0x00ffbf, 0x00ffff,
  0x330000, 0x33003f, 0x33007f, 0x3300bf, 0x3300ff, 0x332400, 0x33243f, 0x33247f,
  0x3324bf, 0x3324ff, 0x334800, 0x33483f, 0x33487f, 0x3348bf, 0x3348ff, 0x336d00,
  0x336d3f, 0x336d7f, 0x336dbf, 0x336dff, 0x339100, 0x33913f, 0x33917f, 0x3391bf,
  0x3391ff, 0x33b600, 0x33b63f, 0x33b67f, 0x33b6bf, 0x33b6ff, 0x33da00, 0x33da3f,
  0x33da7f, 0x33dabf, 0x33daff, 0x33ff00, 0x33ff3f, 0x33ff7f, 0x33ffbf, 0x33ffff,
  0x660000, 0x66003f, 0x66007f, 0x6600bf, 0x6600ff, 0x662400, 0x66243f, 0x66247f,
  0x6624bf, 0x6624ff, 0x664800, 0x66483f, 0x66487f, 0x6648bf, 0x6648ff, 0x666d00,
  0x666d3f, 0x666d7f, 0x666dbf, 0x666dff, 0x669100, 0x66913f, 0x66917f, 0x6691bf,
  0x6691ff, 0x66b600, 0x66b63f, 0x66b67f, 0x66b6bf, 0x66b6ff, 0x66da00, 0x66da3f,
  0x66da7f, 0x66dabf, 0x66daff, 0x66ff00, 0x66ff3f, 0x66ff7f, 0x66ffbf, 0x66ffff,
  0x990000, 0x99003f, 0x99007f, 0x9900bf, 0x9900ff, 0x992400, 0x99243f, 0x99247f,
  0x9924bf, 0x9924ff, 0x994800, 0x99483f, 0x99487f, 0x9948bf, 0x9948ff, 0x996d00,
  0x996d3f, 0x996d7f, 0x996dbf, 0x996dff, 0x999100, 0x99913f, 0x99917f, 0x9991bf,
  0x9991ff, 0x99b600, 0x99b63f, 0x99b67f, 0x99b6bf, 0x99b6ff, 0x99da00, 0x99da3f,
  0x99da7f, 0x99dabf, 0x99daff, 0x99ff00, 0x99ff3f, 0x99ff7f, 0x99ffbf, 0x99ffff,
  0xcc0000, 0xcc003f, 0xcc007f, 0xcc00bf, 0xcc00ff, 0xcc2400, 0xcc243f, 0xcc247f,
  0xcc24bf, 0xcc24ff, 0xcc4800, 0xcc483f, 0xcc487f, 0xcc48bf, 0xcc48ff, 0xcc6d00,
  0xcc6d3f, 0xcc6d7f, 0xcc6dbf, 0xcc6dff, 0xcc9100, 0xcc913f, 0xcc917f, 0xcc91bf,
  0xcc91ff, 0xccb600, 0xccb63f, 0xccb67f, 0xccb6bf, 0xccb6ff, 0xccda00, 0xccda3f,
  0xccda7f, 0xccdabf, 0xccdaff, 0xccff00, 0xccff3f, 0xccff7f, 0xccffbf, 0xccffff,
  0xff0000, 0xff003f, 0xff007f, 0xff00bf, 0xff00ff, 0xff2400, 0xff243f, 0xff247f,
  0xff24bf, 0xff24ff, 0xff4800, 0xff483f, 0xff487f, 0xff48bf, 0xff48ff, 0xff6d00,
  0xff6d3f, 0xff6d7f, 0xff6dbf, 0xff6dff, 0xff9100, 0xff913f, 0xff917f, 0xff91bf,
  0xff91ff, 0xffb600, 0xffb63f, 0xffb67f, 0xffb6bf, 0xffb6ff, 0xffda00, 0xffda3f,
  0xffda7f, 0xffdabf, 0xffdaff, 0xffff00, 0xffff3f, 0xffff7f, 0xffffbf, 0xffffff
};
