export * from './authenticationController.service';
import { AuthenticationControllerService } from './authenticationController.service';
export * from './reservationController.service';
import { ReservationControllerService } from './reservationController.service';
export * from './userController.service';
import { UserControllerService } from './userController.service';
export const APIS = [AuthenticationControllerService, ReservationControllerService, UserControllerService];
