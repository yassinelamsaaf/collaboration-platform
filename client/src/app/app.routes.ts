import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth.guard';
import { LoginComponent } from './features/auth/login/login.component';
import { RegisterComponent } from './features/auth/register/register.component';
import { VerifyEmailComponent } from './features/auth/verify-email/verify-email.component';
import { WelcomeComponent } from './pages/welcome/welcome.component';

export const routes: Routes = [
	{ path: '', pathMatch: 'full', redirectTo: 'auth/login' },
	{
		path: 'auth',
		children: [
			{ path: 'login', component: LoginComponent },
			{ path: 'register', component: RegisterComponent },
			{ path: 'verify-email', component: VerifyEmailComponent }
		]
	},
	{
		path: 'welcome',
		component: WelcomeComponent,
		canActivate: [authGuard]
	},
	{ path: '**', redirectTo: 'auth/login' }
];
