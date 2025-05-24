read -rp "Enter the path to glfw source: " glfwPath
echo GLFW Path: $glfwPath

if ! test -d $glfwPath; then
  echo Invalid path
  exit 1
fi

depsPath=$glfwPath/deps/wayland
echo $depsPath

if ! test -d $depsPath; then
  echo Invalid path
  exit 1
fi

srcPath=$glfwPath/src
echo $srcPath

if ! test -d $srcPath; then
  echo Invalid path
  exit 1
fi

# Generated for GLFW 3.4
wayland-scanner client-header $depsPath/wayland.xml $srcPath/wayland-client-protocol.h
wayland-scanner client-header $depsPath/viewporter.xml $srcPath/viewporter-client-protocol.h
wayland-scanner client-header $depsPath/xdg-shell.xml $srcPath/xdg-shell-client-protocol.h
wayland-scanner client-header $depsPath/idle-inhibit-unstable-v1.xml $srcPath/idle-inhibit-unstable-v1-client-protocol.h
wayland-scanner client-header $depsPath/pointer-constraints-unstable-v1.xml $srcPath/pointer-constraints-unstable-v1-client-protocol.h
wayland-scanner client-header $depsPath/relative-pointer-unstable-v1.xml $srcPath/relative-pointer-unstable-v1-client-protocol.h
wayland-scanner client-header $depsPath/fractional-scale-v1.xml $srcPath/fractional-scale-v1-client-protocol.h
wayland-scanner client-header $depsPath/xdg-activation-v1.xml $srcPath/xdg-activation-v1-client-protocol.h
wayland-scanner client-header $depsPath/xdg-decoration-unstable-v1.xml $srcPath/xdg-decoration-unstable-v1-client-protocol.h
wayland-scanner private-code $depsPath/wayland.xml $srcPath/wayland-client-protocol-code.h
wayland-scanner private-code $depsPath/viewporter.xml $srcPath/viewporter-client-protocol-code.h
wayland-scanner private-code $depsPath/xdg-shell.xml $srcPath/xdg-shell-client-protocol-code.h
wayland-scanner private-code $depsPath/idle-inhibit-unstable-v1.xml $srcPath/idle-inhibit-unstable-v1-client-protocol-code.h
wayland-scanner private-code $depsPath/pointer-constraints-unstable-v1.xml $srcPath/pointer-constraints-unstable-v1-client-protocol-code.h
wayland-scanner private-code $depsPath/relative-pointer-unstable-v1.xml $srcPath/relative-pointer-unstable-v1-client-protocol-code.h
wayland-scanner private-code $depsPath/fractional-scale-v1.xml $srcPath/fractional-scale-v1-client-protocol-code.h
wayland-scanner private-code $depsPath/xdg-activation-v1.xml $srcPath/xdg-activation-v1-client-protocol-code.h
wayland-scanner private-code $depsPath/xdg-decoration-unstable-v1.xml $srcPath/xdg-decoration-unstable-v1-client-protocol-code.h