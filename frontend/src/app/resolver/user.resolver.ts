import {ResolveFn} from '@angular/router';
import {inject} from '@angular/core';
import {UserControllerService, UserDTO} from '../api';

export const userResolver: ResolveFn<UserDTO> = (route, state) => {
  return inject(UserControllerService).getUser();
};
