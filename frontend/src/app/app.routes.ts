import {Routes} from '@angular/router';
import {LoginComponent} from './login/login.component';
import {HomeComponent} from './home/home.component';
import {userResolver} from './resolver/user.resolver';
import {ErrorComponent} from './error/error.component';

export const routes: Routes = [
  {path: 'login', component: LoginComponent},
  {
    path: 'home',
    component: HomeComponent,
    resolve: {
      user: userResolver
    }
  },
  {path: 'error', component: ErrorComponent}
];
