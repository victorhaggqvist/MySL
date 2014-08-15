My SL
=====
A unofficial Android application for Mitt SL, https://sl.se/sv/mitt-sl/.

####Freatures
- View Access Cards
- View Online orders
- Create account
- Add card
- Get notification when card expires

**Permissions**
Full network access: Needed to connect to SL.se
View network connections: Demiter if connection is possible
Run at startup: Only used if you set notifications, needed for scheduling notifications

#Use cases
Case | Available | Tested
:-------|:-------|:--
My SL login | yes | yes
View Accesscards | yes | yes
View onlile orders | yes | yes
View purse value | yes | yes
View purse standard ride | yes | yes
Create my sl account | yes | **no**
Add accesscard | yes | **somewhat**, add already added card is tested
View period ticket (generic) | yes | **somewhat**, all types not tested
Add period ticket | no | **no**<sup>1</sup>
Add purse | no | **no**<sup>1</sup>
View purse transactions | no | **no**
Report loss of card | no | **no**
Rename card | no | **no**

**Note 1**: Any action that involves money  are left out on purpose, for now.

###Period ticket cases
Tested | Case | Edges | Tested Ticket
--|:-----|:---------|:--
yes | Regular period | Start and stop set. Ticket valid. Student ticket. | 30 day (student)
yes | Inactive | Start and stop NOT set. Ticket valid. Student ticket. | 30 day (student)
yes | Multiple tickets | Two tickets in array | 30 day (student) + 30 day inactive (student)
no | Arlanda enabled ticket | A ticket that is enabled to get off at Arlanda at no additional charge | -
no | UL enabled ticket | Don't if given data is different, since the ticket apprers to be a receipt (ref https://sl.se/sv/kop-biljett/) | -
no | Area ticket | Like 30-dagarsbiljett Norrtälje | -
no | Summer ticket | Vaild through 1 may - 31 august, don't know if different from regular or just ohter name. | -

###Other tickets
- Don't know if One-Time cards classify as AccessCards, if so that needs testing too.

#Todo
- Add view for purse transactions
- Add ability to reset account password from within app?
- Add monitoring of purse, to add notification when it goes empty
- (Hope that SL doesn't change their APIs in the near future)

#License
	MySL - A unofficiall Android client for the "Mitt SL" part of http://sl.se
    Copyright (C) 2014  Victor Häggqvist

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.