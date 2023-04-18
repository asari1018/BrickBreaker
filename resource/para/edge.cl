//19B11212 Arisa Kohtani
#pragma OPENCL EXTENSION cl_khr_byte_addressable_store : enable

int addr(const int width, const int height, int x, int y){
  if(y<0){y=0;}
  if(height-1<y){y=height-1;}
  if(x<0){x=0;}
  if(width-1<x){x=width-1;}
  return (y*width*3+x*3);
}

float bound(const float in){
  if(in<0) return 0; 
  if(in>255) return 255.0f;
  return in;
}

float filterA(__global const uchar* in, const int width, const int height,
           const int lx,const int ly, const int shift){
  return (
          in[addr(width, height, lx-1, ly-1)+shift] * (-1)+
          in[addr(width, height, lx  , ly-1)+shift] * 0+
          in[addr(width, height, lx+1, ly-1)+shift] * 1+

          in[addr(width, height, lx-1, ly)+shift] * (-2)+
          in[addr(width, height, lx  , ly)+shift] * 0+
          in[addr(width, height, lx+1, ly)+shift] * 2+

          in[addr(width, height, lx-1, ly+1)+shift] * (-1)+
          in[addr(width, height, lx  , ly+1)+shift] * 0+
          in[addr(width, height, lx+1, ly+1)+shift] * 1
          );
}

float filterB(__global const uchar* in, const int width, const int height,
           const int lx,const int ly, const int shift){
  return (
          in[addr(width, height, lx-1, ly-1)+shift] * (-1)+
          in[addr(width, height, lx  , ly-1)+shift] * (-2)+
          in[addr(width, height, lx+1, ly-1)+shift] * (-1)+

          in[addr(width, height, lx-1, ly)+shift] * 0+
          in[addr(width, height, lx  , ly)+shift] * 0+
          in[addr(width, height, lx+1, ly)+shift] * 0+

          in[addr(width, height, lx-1, ly+1)+shift] * 1+
          in[addr(width, height, lx  , ly+1)+shift] * 2+
          in[addr(width, height, lx+1, ly+1)+shift] * 1
          );
}


// OpenCL Kernel Function 
__kernel void Filter(const int width, const int height, 
                     __global const uchar* in, 
                     __global uchar *outb,
		     const float scale) {
  // get index of global data array
  int lx = get_global_id(0);
  int ly = get_global_id(1);

  int oadd = (ly*width+lx)*4;
  float A_C_0 = filterA(in,width,height,lx,ly,0)*filterA(in,width,height,lx,ly,0);
  float A_C_1 = filterA(in,width,height,lx,ly,1)*filterA(in,width,height,lx,ly,1);
  float A_C_2 = filterA(in,width,height,lx,ly,2)*filterA(in,width,height,lx,ly,2);

  float B_C_0 = filterB(in,width,height,lx,ly,0)*filterB(in,width,height,lx,ly,0);
  float B_C_1 = filterB(in,width,height,lx,ly,1)*filterB(in,width,height,lx,ly,1);
  float B_C_2 = filterB(in,width,height,lx,ly,2)*filterB(in,width,height,lx,ly,2);

  outb[oadd  ]= bound((A_C_0+B_C_0)*scale/1600);
  outb[oadd+1]= bound((A_C_1+B_C_1)*scale/1600);
  outb[oadd+2]= bound((A_C_2+B_C_2)*scale/1600);
  outb[oadd+3]= 255;
}

