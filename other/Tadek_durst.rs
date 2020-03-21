use num::Num;

pub trait Sample: Copy + Num {}

pub struct StreamState<T: Sample> {
  buffer: Vec<T>,
  buffer_start: usize,
  pop_points: Vec<usize>
}

pub fn create_stream<T: Sample>() -> *mut StreamState<T> {
  let result = StreamState {
    buffer: Vec::<T>::new(),
    buffer_start: 0,
    pop_points: Vec::<usize>::new()
  };

  Box::into_raw(Box::new(result))
}

pub fn put<T: Sample>(state_pointer: *mut StreamState<T>, input_buffer: &Vec<T>) {
  let state = unsafe { &mut (*state_pointer) };

  state.buffer.append(&mut input_buffer.clone());
}

pub fn take<T: Sample>(state_pointer: *mut StreamState<T>, length: usize) {
  let state = unsafe { &mut (*state_pointer) };

  let drain_length = length.min(state.buffer.len());

  state.buffer_start += drain_length;

  // let result = {
  //   //let result = state.buffer.drain(0..drain_length).map();

  //   result
  // };

  ()
}
