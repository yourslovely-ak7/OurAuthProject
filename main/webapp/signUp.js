
async function createUser()
{
	const password= document.getElementById('password').value;
	const confirmPassword= document.getElementById('confirmPassword').value;
	
	if(password === confirmPassword && password.length !==0)
	{
		const name= document.getElementById('name').value;
		const firstName= document.getElementById('fname').value;
		const lastName= document.getElementById('lname').value;
		const email= document.getElementById('email').value;
		const gender = document.querySelector('input[name="gender"]:checked')?.value || null;
		
		const response= await fetch('/OurAuth/account?type=signup', {
			method: 'POST',
			body: JSON.stringify({name, email, password, gender, lastName, firstName})
		});
		
		if(response.ok)
		{
			alert('Account created successfully!');
			window.location.href= '/OurAuth/login.html';
		}
	}
	else
	{
		alert('Both Password and Confirm Password should be same!');
	}
	
}