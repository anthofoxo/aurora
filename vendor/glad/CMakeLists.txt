cmake_minimum_required(VERSION 3.10)
project(glad LANGUAGES C)
add_library(glad src/gl.c)
target_include_directories(glad PUBLIC include)